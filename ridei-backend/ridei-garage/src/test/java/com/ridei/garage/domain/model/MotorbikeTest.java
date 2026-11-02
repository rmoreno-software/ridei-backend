package com.ridei.garage.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ridei.garage.domain.exception.CannotActivateRetiredMotorbikeException;
import com.ridei.garage.domain.exception.PhotoNotFoundException;

class MotorbikeTest {

    private static final OwnerId OWNER = new OwnerId(UUID.randomUUID());

    static Motorbike motorbike(Category category, boolean active, LocalDate disposalDate) {
        return Motorbike.register(
            OWNER, null, " Yamaha ", " R1 ", category, 2020, 998, null,
            LocalDate.of(2021, 1, 1), disposalDate, active
        );
    }

    @Test
    void registerTrimsTextAndStartsActiveOnlyWhenRequested() {
        Motorbike motorbike = motorbike(Category.SPEED, true, null);

        assertThat(motorbike.getBrandName()).isEqualTo("Yamaha");
        assertThat(motorbike.getModel()).isEqualTo("R1");
        assertThat(motorbike.isActive()).isTrue();
        assertThat(motorbike(Category.SPEED, false, null).isActive()).isFalse();
    }

    @Test
    void retiredMotorbikeCannotBeRegisteredAsActive() {
        assertThatThrownBy(() -> motorbike(Category.SPEED, true, LocalDate.of(2022, 1, 1)))
            .isInstanceOf(CannotActivateRetiredMotorbikeException.class);
    }

    @Test
    void yearBeforeFirstMotorbikeIsRejected() {
        assertThatThrownBy(() -> Motorbike.register(
            OWNER, null, "Brand", "Model", Category.SPEED, Motorbike.FIRST_MOTORBIKE_YEAR - 1,
            null, null, null, null, false
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith("Year must be between " + Motorbike.FIRST_MOTORBIKE_YEAR);
    }

    @Test
    void settingDisposalDateOnUpdateDeactivates() {
        Motorbike motorbike = motorbike(Category.SPEED, true, null);

        motorbike.update(null, "Yamaha", "R1", Category.SPEED, 2020, 998, null,
            LocalDate.of(2021, 1, 1), LocalDate.of(2022, 1, 1));

        assertThat(motorbike.isActive()).isFalse();
        assertThat(motorbike.isRetired()).isTrue();
    }

    @Test
    void firstPhotoBecomesPrimaryAndDuplicatesAreRejected() {
        Motorbike motorbike = motorbike(Category.SPEED, false, null);

        motorbike.addPhoto("https://cdn/a.jpg", null);
        motorbike.addPhoto("https://cdn/b.jpg", new FocalPoint(0.2, 0.8));

        assertThat(motorbike.primaryPhoto()).get().extracting(MotorbikePhoto::getUrl).isEqualTo("https://cdn/a.jpg");
        assertThat(motorbike.getPhotos().get(0).getFocalPoint()).isEqualTo(FocalPoint.CENTER);
        assertThatThrownBy(() -> motorbike.addPhoto("https://cdn/a.jpg", null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setPrimaryPhotoLeavesExactlyOnePrimary() {
        Motorbike motorbike = motorbike(Category.SPEED, false, null);
        motorbike.addPhoto("https://cdn/a.jpg", null);
        motorbike.addPhoto("https://cdn/b.jpg", null);
        PhotoId second = motorbike.getPhotos().get(1).getId();

        motorbike.setPrimaryPhoto(second);

        assertThat(motorbike.getPhotos()).filteredOn(MotorbikePhoto::isPrimary)
            .singleElement().extracting(MotorbikePhoto::getId).isEqualTo(second);
    }

    @Test
    void removingPrimaryPhotoPromotesRemainingOne() {
        Motorbike motorbike = motorbike(Category.SPEED, false, null);
        motorbike.addPhoto("https://cdn/a.jpg", null);
        motorbike.addPhoto("https://cdn/b.jpg", null);
        PhotoId primary = motorbike.primaryPhoto().orElseThrow().getId();

        MotorbikePhoto removed = motorbike.removePhoto(primary);

        assertThat(removed.getUrl()).isEqualTo("https://cdn/a.jpg");
        assertThat(motorbike.primaryPhoto()).get().extracting(MotorbikePhoto::getUrl).isEqualTo("https://cdn/b.jpg");
    }

    @Test
    void unknownPhotoIsReportedAsNotFound() {
        Motorbike motorbike = motorbike(Category.SPEED, false, null);

        assertThatThrownBy(() -> motorbike.removePhoto(PhotoId.newId()))
            .isInstanceOf(PhotoNotFoundException.class);
    }
}
