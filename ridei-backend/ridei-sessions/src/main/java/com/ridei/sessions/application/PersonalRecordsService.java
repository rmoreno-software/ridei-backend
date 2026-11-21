package com.ridei.sessions.application;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.sessions.application.port.in.GetPersonalRecordsUseCase;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.PersonalRecord;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.service.PersonalRecords;

public class PersonalRecordsService implements GetPersonalRecordsUseCase {

    private final SessionRepositoryPort sessionRepository;

    public PersonalRecordsService(SessionRepositoryPort sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalRecord> records(RiderId riderId, LayoutId layoutId, MotorbikeId motorbikeId) {
        return PersonalRecords.of(sessionRepository.findByRider(riderId)).stream()
            .filter(r -> layoutId == null || r.layout().layoutId().equals(layoutId))
            .filter(r -> motorbikeId == null || r.motorbike().motorbikeId().equals(motorbikeId))
            .toList();
    }
}
