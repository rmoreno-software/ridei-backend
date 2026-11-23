package com.ridei.sessions.domain.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.ManualLapEntry;
import com.ridei.sessions.domain.model.TrackCondition;

/**
 * Convierte las notas que el piloto pega (pantalla 22C) en tiempos: una línea por tanda, con
 * circuito, fecha y tiempo en cualquier orden, tal cual los apuntó.
 *
 * <pre>
 * Calafat 12/05/24 1:45.3
 * Castellolí 2/6/24 1:14,22 (lluvia)
 * Montmeló nacional 20-9-24 1.26.10
 * Alcarras 3/3/23 1:52
 * </pre>
 *
 * <ul>
 *   <li>Fechas día/mes/año con {@code /}, {@code -} o {@code .}, año de 2 o 4 cifras, o ISO
 *       {@code 2024-05-12}.</li>
 *   <li>Tiempos {@code m:ss}, {@code m:ss.d…}, {@code m.ss.d…} o {@code ss.d…}, con punto o coma
 *       decimal y de 0 a 3 decimales. La precisión es la que se escribió. Si una línea tiene
 *       varios, el más rápido es la mejor vuelta y el resto "otras vueltas".</li>
 *   <li>Condiciones por palabra clave (lluvia, mojado, húmedo, seco… en castellano, catalán o
 *       inglés), sueltas o entre paréntesis. Lo demás entre paréntesis pasa a notas.</li>
 *   <li>Lo que queda con letras es el texto del circuito, que luego se busca en el catálogo.</li>
 * </ul>
 *
 * Nunca falla por una línea: lo que no entiende lo marca como problema para revisarlo.
 */
public final class ManualTimeNotesParser {

    public static final int MAX_LINES = 100;

    private static final Pattern SLASH_DATE = Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4}|\\d{2})");
    private static final Pattern ISO_DATE = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
    private static final Pattern DOT_DATE = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4}|\\d{2})");
    private static final Pattern TIME_COLON = Pattern.compile("(\\d{1,2})[:'](\\d{2})(?:[.,:\"](\\d{1,3}))?");
    private static final Pattern TIME_DOTS = Pattern.compile("(\\d{1,2})\\.(\\d{2})[.,](\\d{1,3})");
    private static final Pattern TIME_SECONDS = Pattern.compile("(\\d{1,2})[.,](\\d{1,3})");
    private static final Pattern PARENTHESES = Pattern.compile("\\(([^)]*)\\)");
    private static final Pattern EDGE_PUNCTUATION = Pattern.compile("^[,;|]+|[,;|]+$");

    private static final Map<String, TrackCondition> CONDITION_WORDS = Map.ofEntries(
        Map.entry("lluvia", TrackCondition.WET), Map.entry("mojado", TrackCondition.WET), Map.entry("mojada", TrackCondition.WET),
        Map.entry("pluja", TrackCondition.WET), Map.entry("mullat", TrackCondition.WET), Map.entry("mullada", TrackCondition.WET),
        Map.entry("wet", TrackCondition.WET), Map.entry("rain", TrackCondition.WET),
        Map.entry("humedo", TrackCondition.DAMP), Map.entry("humeda", TrackCondition.DAMP),
        Map.entry("humit", TrackCondition.DAMP), Map.entry("humida", TrackCondition.DAMP), Map.entry("damp", TrackCondition.DAMP),
        Map.entry("seco", TrackCondition.DRY), Map.entry("seca", TrackCondition.DRY), Map.entry("sec", TrackCondition.DRY),
        Map.entry("dry", TrackCondition.DRY));

    public enum Problem {
        MISSING_CIRCUIT,
        MISSING_DATE,
        /** En el futuro o antes de {@link ManualLapEntry#EARLIEST_DATE}. */
        INVALID_DATE,
        MISSING_TIME,
        /** Algún tiempo fuera de 10 s – 60 min: se descarta. */
        TIME_OUT_OF_RANGE
    }

    /**
     * @param circuitText     lo que queda para buscar en el catálogo; {@code null} si nada
     * @param bestLapMillis   {@code null} si no hay ningún tiempo válido
     * @param otherLapsMillis resto de tiempos de la línea, en el orden escrito
     * @param note            texto entre paréntesis que no es una condición
     */
    public record ParsedLine(
        int lineNumber,
        String text,
        String circuitText,
        LocalDate date,
        Long bestLapMillis,
        LapTimePrecision precision,
        List<Long> otherLapsMillis,
        TrackCondition trackCondition,
        String note,
        List<Problem> problems
    ) {
        public ParsedLine {
            otherLapsMillis = List.copyOf(otherLapsMillis);
            problems = List.copyOf(problems);
        }

        public boolean isComplete() {
            return problems.isEmpty();
        }
    }

    private record LapTime(long millis, LapTimePrecision precision) {}

    private ManualTimeNotesParser() {}

    /**
     * @param today hoy en UTC, para descartar fechas futuras (con un día de margen por la zona
     *              horaria) y decidir el siglo de los años de 2 cifras
     */
    public static List<ParsedLine> parse(String text, LocalDate today) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Notes are required");
        String[] rawLines = text.split("\\R");
        List<ParsedLine> parsed = new ArrayList<>();
        for (int i = 0; i < rawLines.length; i++) {
            if (rawLines[i].isBlank()) continue;
            if (parsed.size() == MAX_LINES) throw new IllegalArgumentException("At most " + MAX_LINES + " lines per import");
            parsed.add(parseLine(i + 1, rawLines[i].strip(), today));
        }
        if (parsed.isEmpty()) throw new IllegalArgumentException("Notes are required");
        return parsed;
    }

    static ParsedLine parseLine(int lineNumber, String line, LocalDate today) {
        TrackCondition condition = null;
        List<String> noteParts = new ArrayList<>();

        // 1. Paréntesis: condiciones y notas.
        Matcher parentheses = PARENTHESES.matcher(line);
        while (parentheses.find()) {
            List<String> rest = new ArrayList<>();
            for (String word : parentheses.group(1).strip().split("\\s+")) {
                TrackCondition c = CONDITION_WORDS.get(TextNormalizer.normalize(word));
                if (c != null) condition = c;
                else if (!word.isBlank()) rest.add(word);
            }
            if (!rest.isEmpty()) noteParts.add(String.join(" ", rest));
        }
        String withoutParentheses = PARENTHESES.matcher(line).replaceAll(" ");

        // 2. Fichas sueltas.
        LocalDate date = null;
        boolean dateIsDotted = false;
        List<String> ambiguous = new ArrayList<>();   // 12.05.24: ¿fecha o 12:05.24?
        List<String> timeTokens = new ArrayList<>();
        List<String> circuitWords = new ArrayList<>();
        for (String raw : withoutParentheses.strip().split("\\s+")) {
            String token = EDGE_PUNCTUATION.matcher(raw).replaceAll("");
            if (token.isEmpty()) continue;
            LocalDate strongDate = strongDate(token, today);
            if (strongDate != null) {
                if (date == null || dateIsDotted) {
                    date = strongDate;
                    dateIsDotted = false;
                }
                continue;
            }
            boolean dotDate = dottedDate(token, today) != null;
            boolean time = lapTime(token) != null;
            if (dotDate && time) ambiguous.add(token);
            else if (dotDate && date == null) {
                date = dottedDate(token, today);
                dateIsDotted = true;
            } else if (time) timeTokens.add(token);
            else {
                TrackCondition c = CONDITION_WORDS.get(TextNormalizer.normalize(token));
                if (c != null) condition = c;
                else if (token.codePoints().anyMatch(Character::isLetter)) circuitWords.add(token);
            }
        }
        // Las ambiguas son tiempos salvo que falte la fecha y haya otro tiempo con el que no dudar.
        for (String token : ambiguous) {
            if (date == null && (!timeTokens.isEmpty() || ambiguous.size() > 1)) {
                date = dottedDate(token, today);
            } else {
                timeTokens.add(token);
            }
        }

        // 3. Tiempos.
        List<Problem> problems = new ArrayList<>();
        List<LapTime> times = new ArrayList<>();
        boolean outOfRange = false;
        for (String token : timeTokens) {
            LapTime t = lapTime(token);
            if (t.millis() < ManualLapEntry.MIN_LAP_MILLIS || t.millis() > ManualLapEntry.MAX_LAP_MILLIS) outOfRange = true;
            else times.add(t);
        }
        LapTime best = times.stream().min(Comparator.comparingLong(LapTime::millis)).orElse(null);
        List<Long> others = new ArrayList<>();
        if (best != null) {
            boolean skipped = false;
            for (LapTime t : times) {
                if (!skipped && t == best) {
                    skipped = true;
                    continue;
                }
                others.add(t.millis());
            }
        }

        String circuitText = circuitWords.isEmpty() ? null : String.join(" ", circuitWords);
        if (circuitText == null) problems.add(Problem.MISSING_CIRCUIT);
        if (date == null) problems.add(Problem.MISSING_DATE);
        else if (date.isAfter(today.plusDays(1)) || date.isBefore(ManualLapEntry.EARLIEST_DATE)) problems.add(Problem.INVALID_DATE);
        if (best == null && !outOfRange) problems.add(Problem.MISSING_TIME);
        if (outOfRange) problems.add(Problem.TIME_OUT_OF_RANGE);

        return new ParsedLine(
            lineNumber, line, circuitText, date,
            best != null ? best.millis() : null, best != null ? best.precision() : null, others,
            condition, noteParts.isEmpty() ? null : String.join("; ", noteParts), problems);
    }

    private static LocalDate strongDate(String token, LocalDate today) {
        Matcher m = SLASH_DATE.matcher(token);
        if (m.matches()) return date(m.group(3), m.group(2), m.group(1), today);
        m = ISO_DATE.matcher(token);
        if (m.matches()) return date(m.group(1), m.group(2), m.group(3), today);
        return null;
    }

    private static LocalDate dottedDate(String token, LocalDate today) {
        Matcher m = DOT_DATE.matcher(token);
        return m.matches() ? date(m.group(3), m.group(2), m.group(1), today) : null;
    }

    private static LocalDate date(String year, String month, String day, LocalDate today) {
        int y = Integer.parseInt(year);
        if (year.length() == 2) {
            y += 2000;
            if (y > today.getYear()) y -= 100;
        }
        try {
            return LocalDate.of(y, Integer.parseInt(month), Integer.parseInt(day));
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static LapTime lapTime(String token) {
        Matcher m = TIME_COLON.matcher(token);
        if (m.matches()) return lapTime(m.group(1), m.group(2), m.group(3));
        m = TIME_DOTS.matcher(token);
        if (m.matches()) return lapTime(m.group(1), m.group(2), m.group(3));
        m = TIME_SECONDS.matcher(token);
        if (m.matches()) return lapTime("0", m.group(1), m.group(2));
        return null;
    }

    private static LapTime lapTime(String minutes, String seconds, String fraction) {
        int s = Integer.parseInt(seconds);
        if (s >= 60) return null;
        int digits = fraction == null ? 0 : fraction.length();
        long fractionMillis = fraction == null ? 0 : Long.parseLong(fraction) * (long) Math.pow(10, 3 - digits);
        long millis = (Integer.parseInt(minutes) * 60L + s) * 1000 + fractionMillis;
        return new LapTime(millis, LapTimePrecision.ofFractionDigits(digits));
    }
}
