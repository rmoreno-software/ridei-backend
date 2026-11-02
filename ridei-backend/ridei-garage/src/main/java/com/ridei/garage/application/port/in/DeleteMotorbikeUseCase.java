package com.ridei.garage.application.port.in;

import com.ridei.garage.application.DeleteMotorbikeCommand;

public interface DeleteMotorbikeUseCase {
    void delete(DeleteMotorbikeCommand command);
}
