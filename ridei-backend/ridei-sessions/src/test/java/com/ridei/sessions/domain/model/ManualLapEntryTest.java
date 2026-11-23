package com.ridei.sessions.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class ManualLapEntryTest {

    static final LocalDate DATE = LocalDate.of(2024, 5, 12);

    @Test
    void infersThePrecisionWhenNotGivenAndNormalizesBlanks() {
        ManualLapEntry entry = new ManualLapEntry(DATE, 105_300, null, TimeSource.LAPTIMER, null, null, " ", null, "");

        assertThat(entry.precision()).isEqualTo(LapTimePrecision.TENTHS);
        assertThat(entry.tyres()).isNull();
        assertThat(entry.notes()).isNull();
        assertThat(entry.otherLapsMillis()).isEmpty();
    }

    @Test
    void rejectsInconsistentData() {
        assertThatThrownBy(() -> entry(105_310, LapTimePrecision.TENTHS, null, List.of()))
            .hasMessageContaining("precision");
        assertThatThrownBy(() -> entry(5_000, null, null, List.of())).hasMessageContaining("between 10 s and 60 min");
        assertThatThrownBy(() -> entry(105_300, null, null, List.of(104_000L))).hasMessageContaining("faster than the best");
        assertThatThrownBy(() -> entry(105_300, null, 2, List.of(106_000L, 107_000L))).hasMessageContaining("Lap count");
        assertThatThrownBy(() -> new ManualLapEntry(LocalDate.of(1949, 1, 1), 105_300, null, TimeSource.LAPTIMER, null, null, null, null, null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ManualLapEntry(DATE, 105_300, null, null, null, null, null, null, null))
            .hasMessageContaining("source");
    }

    static ManualLapEntry entry(long millis, LapTimePrecision precision, Integer lapCount, List<Long> others) {
        return new ManualLapEntry(DATE, millis, precision, TimeSource.LAPTIMER, lapCount, TrackCondition.DRY, null, others, null);
    }
}
