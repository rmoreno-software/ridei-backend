package com.ridei.garage.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.OwnerId;

class MotorbikeActivationPolicyTest {

    private static final OwnerId OWNER = new OwnerId(UUID.randomUUID());

    private static Motorbike motorbike(Category category, boolean active, LocalDate disposalDate) {
        return Motorbike.register(OWNER, null, "Brand", "Model", category, 2020, null, null,
            null, disposalDate, active);
    }

    @Test
    void deactivatesOnlyActiveSiblingsOfTheSameGroup() {
        Motorbike target = motorbike(Category.SPEED, true, null);
        Motorbike sameGroupActive = motorbike(Category.NAKED, true, null);
        Motorbike otherGroupActive = motorbike(Category.ENDURO, true, null);
        Motorbike sameGroupInactive = motorbike(Category.CLASSIC, false, null);

        List<Motorbike> deactivated = MotorbikeActivationPolicy.deactivateConflictsWith(
            target, List.of(target, sameGroupActive, otherGroupActive, sameGroupInactive));

        assertThat(deactivated).containsExactly(sameGroupActive);
        assertThat(sameGroupActive.isActive()).isFalse();
        assertThat(otherGroupActive.isActive()).isTrue();
        assertThat(target.isActive()).isTrue();
    }

    @Test
    void soleCandidateIgnoresRetiredAndOtherGroups() {
        Motorbike target = motorbike(Category.SPEED, false, null);
        Motorbike retiredSameGroup = motorbike(Category.NAKED, false, LocalDate.of(2022, 1, 1));
        Motorbike otherGroup = motorbike(Category.ENDURO, false, null);

        assertThat(MotorbikeActivationPolicy.isSoleCandidateInGroup(
            target, List.of(target, retiredSameGroup, otherGroup))).isTrue();
        assertThat(MotorbikeActivationPolicy.isSoleCandidateInGroup(
            target, List.of(target, motorbike(Category.NAKED, false, null)))).isFalse();
    }
}
