package com.ridei.garage.domain.port.in;

import java.util.List;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.OwnerId;

public interface ListMyMotorbikesUseCase {
    List<Motorbike> list(OwnerId ownerId);
}
