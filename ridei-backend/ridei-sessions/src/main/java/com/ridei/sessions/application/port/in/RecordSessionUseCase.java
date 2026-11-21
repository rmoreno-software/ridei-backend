package com.ridei.sessions.application.port.in;

import com.ridei.sessions.application.RecordSessionCommand;
import com.ridei.sessions.application.RecordSessionResult;

public interface RecordSessionUseCase {
    RecordSessionResult record(RecordSessionCommand command);
}
