package com.ridei.circuits.application;

import java.util.function.Supplier;

import com.ridei.circuits.application.port.in.AddLayoutToTrackUseCase;
import com.ridei.circuits.application.port.in.AddTrackToVenueUseCase;
import com.ridei.circuits.application.port.in.ProposeVenueUseCase;
import com.ridei.circuits.domain.exception.CatalogItemAlreadyExistsException;
import com.ridei.circuits.domain.exception.ConcurrentCatalogModificationException;

/**
 * Decoradores de los casos de uso de alta. Si la base de datos rechaza la escritura porque
 * otra transacción ganó la carrera (UNIQUE o versión optimista), se reintenta una vez en una
 * transacción nueva: ahora el dominio ve lo que la otra creó y responde con el existente o
 * con el 409 de duplicado correspondiente, nunca con un 500.
 *
 * <p>Deben envolver al bean transaccional (no al revés) para que cada intento tenga su propia
 * transacción.
 */
public final class RetryingUseCases {

    private RetryingUseCases() {
    }

    public static ProposeVenueUseCase proposeVenue(ProposeVenueUseCase delegate) {
        return command -> retryOnceOnConcurrentWrite(() -> delegate.propose(command));
    }

    public static AddTrackToVenueUseCase addTrack(AddTrackToVenueUseCase delegate) {
        return command -> retryOnceOnConcurrentWrite(() -> delegate.addTrack(command));
    }

    public static AddLayoutToTrackUseCase addLayout(AddLayoutToTrackUseCase delegate) {
        return command -> retryOnceOnConcurrentWrite(() -> delegate.addLayout(command));
    }

    static <T> T retryOnceOnConcurrentWrite(Supplier<T> attempt) {
        try {
            return attempt.get();
        } catch (CatalogItemAlreadyExistsException | ConcurrentCatalogModificationException lostRace) {
            return attempt.get();
        }
    }
}
