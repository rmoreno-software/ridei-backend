package com.ridei.circuits.application.port.in;

import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.Venue;

public interface VerifyLayoutUseCase {

    Venue verify(LayoutId layoutId);
}
