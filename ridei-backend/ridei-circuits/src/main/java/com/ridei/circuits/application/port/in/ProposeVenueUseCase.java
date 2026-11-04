package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.ProposeVenueCommand;
import com.ridei.circuits.application.ProposeVenueResult;

public interface ProposeVenueUseCase {

    ProposeVenueResult propose(ProposeVenueCommand command);
}
