package com.ridei.sessions.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.TrackCondition;
import com.ridei.sessions.domain.service.ManualTimeNotesParser.ParsedLine;
import com.ridei.sessions.domain.service.ManualTimeNotesParser.Problem;

public class ManualTimeNotesParserTest {

    static final LocalDate TODAY = LocalDate.of(2026, 11, 28);

    /** Las notas de la pantalla 22C, tal cual. */
    public static final String NOTES = """
        Calafat 12/05/24 1:45.3
        Castellolí 2/6/24 1:14,22 (lluvia)
        Montmeló nacional 20-9-24 1.26.10
        Alcarras 3/3/23 1:52
        """;

    @Test
    void parsesTheNotesOfTheDesign() {
        List<ParsedLine> lines = ManualTimeNotesParser.parse(NOTES, TODAY);

        assertThat(lines).hasSize(4).allMatch(ParsedLine::isComplete);

        assertThat(lines.get(0)).satisfies(l -> {
            assertThat(l.circuitText()).isEqualTo("Calafat");
            assertThat(l.date()).isEqualTo(LocalDate.of(2024, 5, 12));
            assertThat(l.bestLapMillis()).isEqualTo(105_300);
            assertThat(l.precision()).isEqualTo(LapTimePrecision.TENTHS);
            assertThat(l.trackCondition()).isNull();
        });
        assertThat(lines.get(1)).satisfies(l -> {
            assertThat(l.circuitText()).isEqualTo("Castellolí");
            assertThat(l.date()).isEqualTo(LocalDate.of(2024, 6, 2));
            assertThat(l.bestLapMillis()).isEqualTo(74_220);
            assertThat(l.precision()).isEqualTo(LapTimePrecision.HUNDREDTHS);
            assertThat(l.trackCondition()).isEqualTo(TrackCondition.WET);
            assertThat(l.note()).isNull();
        });
        assertThat(lines.get(2)).satisfies(l -> {
            assertThat(l.circuitText()).isEqualTo("Montmeló nacional");
            assertThat(l.date()).isEqualTo(LocalDate.of(2024, 9, 20));
            assertThat(l.bestLapMillis()).isEqualTo(86_100);
            assertThat(l.precision()).isEqualTo(LapTimePrecision.HUNDREDTHS);
        });
        assertThat(lines.get(3)).satisfies(l -> {
            assertThat(l.circuitText()).isEqualTo("Alcarras");
            assertThat(l.date()).isEqualTo(LocalDate.of(2023, 3, 3));
            assertThat(l.bestLapMillis()).isEqualTo(112_000);
            assertThat(l.precision()).isEqualTo(LapTimePrecision.SECONDS);
        });
    }

    @Test
    void acceptsAnyOrderIsoDatesAndSeveralLaps() {
        ParsedLine line = ManualTimeNotesParser.parseLine(1, "1:47.2 2024-05-12 Calafat, 1:45.300 1:46.0 (R6 nueva) seco", TODAY);

        assertThat(line.isComplete()).isTrue();
        assertThat(line.circuitText()).isEqualTo("Calafat");
        assertThat(line.date()).isEqualTo(LocalDate.of(2024, 5, 12));
        assertThat(line.bestLapMillis()).isEqualTo(105_300);
        assertThat(line.precision()).isEqualTo(LapTimePrecision.THOUSANDTHS);
        assertThat(line.otherLapsMillis()).containsExactly(107_200L, 106_000L);
        assertThat(line.trackCondition()).isEqualTo(TrackCondition.DRY);
        assertThat(line.note()).isEqualTo("R6 nueva");
    }

    @Test
    void readsDottedDatesWhenThereIsAnotherTimeAndKartingSecondsOnlyTimes() {
        ParsedLine line = ManualTimeNotesParser.parseLine(1, "Karting Vendrell 12.05.24 48.3", TODAY);

        assertThat(line.date()).isEqualTo(LocalDate.of(2024, 5, 12));
        assertThat(line.bestLapMillis()).isEqualTo(48_300);
        assertThat(line.circuitText()).isEqualTo("Karting Vendrell");
    }

    @Test
    void twoDigitYearsAfterThisYearAreLastCentury() {
        ParsedLine line = ManualTimeNotesParser.parseLine(1, "Jarama 3/3/98 1:40", TODAY);

        assertThat(line.date()).isEqualTo(LocalDate.of(1998, 3, 3));
    }

    @Test
    void marksWhatItCannotUnderstandInsteadOfFailing() {
        assertThat(ManualTimeNotesParser.parseLine(1, "Calafat 1:45.3", TODAY).problems()).containsExactly(Problem.MISSING_DATE);
        assertThat(ManualTimeNotesParser.parseLine(1, "Calafat 12/05/24", TODAY).problems()).containsExactly(Problem.MISSING_TIME);
        assertThat(ManualTimeNotesParser.parseLine(1, "12/05/24 1:45.3", TODAY).problems()).containsExactly(Problem.MISSING_CIRCUIT);
        assertThat(ManualTimeNotesParser.parseLine(1, "Calafat 12/05/2027 1:45.3", TODAY).problems()).containsExactly(Problem.INVALID_DATE);
        assertThat(ManualTimeNotesParser.parseLine(1, "Calafat 12/05/24 0:05.1", TODAY).problems()).containsExactly(Problem.TIME_OUT_OF_RANGE);
        assertThat(ManualTimeNotesParser.parseLine(1, "Calafat 31/02/24 1:45.3", TODAY).problems()).containsExactly(Problem.MISSING_DATE);
    }

    @Test
    void skipsBlankLinesAndRejectsEmptyOrHugeNotes() {
        assertThat(ManualTimeNotesParser.parse("\n\nCalafat 12/05/24 1:45.3\n\n", TODAY)).singleElement()
            .extracting(ParsedLine::lineNumber).isEqualTo(3);
        assertThatThrownBy(() -> ManualTimeNotesParser.parse("  \n ", TODAY)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ManualTimeNotesParser.parse("Calafat 12/05/24 1:45.3\n".repeat(101), TODAY))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
