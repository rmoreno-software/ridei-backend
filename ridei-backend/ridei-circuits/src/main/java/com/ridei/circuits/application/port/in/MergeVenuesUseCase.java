package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.MergeVenuesCommand;
import com.ridei.circuits.domain.model.Venue;

public interface MergeVenuesUseCase {

    Venue merge(MergeVenuesCommand command);
}
