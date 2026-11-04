package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.MergeLayoutsCommand;
import com.ridei.circuits.domain.model.Venue;

public interface MergeLayoutsUseCase {

    Venue merge(MergeLayoutsCommand command);
}
