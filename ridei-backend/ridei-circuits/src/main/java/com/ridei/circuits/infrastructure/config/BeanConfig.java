package com.ridei.circuits.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ridei.circuits.application.AddLayoutToTrackService;
import com.ridei.circuits.application.AddTrackToVenueService;
import com.ridei.circuits.application.CorrectCatalogService;
import com.ridei.circuits.application.FindNearbyVenuesService;
import com.ridei.circuits.application.GetVenueDetailService;
import com.ridei.circuits.application.MergeLayoutsService;
import com.ridei.circuits.application.MergeTracksService;
import com.ridei.circuits.application.MergeVenuesService;
import com.ridei.circuits.application.ProposeVenueService;
import com.ridei.circuits.application.RetryingUseCases;
import com.ridei.circuits.application.SearchVenuesService;
import com.ridei.circuits.application.SetDefaultLayoutService;
import com.ridei.circuits.application.VerifyCatalogService;
import com.ridei.circuits.application.port.in.AddLayoutToTrackUseCase;
import com.ridei.circuits.application.port.in.AddTrackToVenueUseCase;
import com.ridei.circuits.application.port.in.FindNearbyVenuesUseCase;
import com.ridei.circuits.application.port.in.GetVenueDetailUseCase;
import com.ridei.circuits.application.port.in.MergeLayoutsUseCase;
import com.ridei.circuits.application.port.in.MergeTracksUseCase;
import com.ridei.circuits.application.port.in.MergeVenuesUseCase;
import com.ridei.circuits.application.port.in.ProposeVenueUseCase;
import com.ridei.circuits.application.port.in.SearchVenuesUseCase;
import com.ridei.circuits.application.port.in.SetDefaultLayoutUseCase;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

/**
 * Cableado manual de los casos de uso (los servicios no llevan anotaciones de Spring).
 *
 * <p>Los casos de uso de alta se exponen envueltos en {@link RetryingUseCases}: el bean del
 * servicio es el proxy transaccional y el decorador ({@code @Primary}) lo llama desde fuera,
 * así cada reintento abre su propia transacción.
 *
 * <p>{@link VerifyCatalogService} y {@link CorrectCatalogService} implementan tres casos de
 * uso cada uno (venue, track, layout) y se inyectan por cualquiera de sus interfaces.
 */
@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // ── Consultas ───────────────────────────────────────────────────────────────────────

    @Bean
    public SearchVenuesUseCase searchVenuesUseCase(VenueRepositoryPort repository) {
        return new SearchVenuesService(repository);
    }

    @Bean
    public FindNearbyVenuesUseCase findNearbyVenuesUseCase(VenueRepositoryPort repository) {
        return new FindNearbyVenuesService(repository);
    }

    @Bean
    public GetVenueDetailUseCase getVenueDetailUseCase(VenueRepositoryPort repository) {
        return new GetVenueDetailService(repository);
    }

    // ── Comandos de usuario ─────────────────────────────────────────────────────────────

    @Bean
    public ProposeVenueService proposeVenueService(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        DeduplicationProperties deduplication,
        Clock clock
    ) {
        return new ProposeVenueService(repository, eventPublisher, deduplication.toPolicy(), clock);
    }

    @Bean
    @Primary
    public ProposeVenueUseCase proposeVenueUseCase(ProposeVenueService service) {
        return RetryingUseCases.proposeVenue(service);
    }

    @Bean
    public AddTrackToVenueService addTrackToVenueService(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new AddTrackToVenueService(repository, eventPublisher, clock);
    }

    @Bean
    @Primary
    public AddTrackToVenueUseCase addTrackToVenueUseCase(AddTrackToVenueService service) {
        return RetryingUseCases.addTrack(service);
    }

    @Bean
    public AddLayoutToTrackService addLayoutToTrackService(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new AddLayoutToTrackService(repository, eventPublisher, clock);
    }

    @Bean
    @Primary
    public AddLayoutToTrackUseCase addLayoutToTrackUseCase(AddLayoutToTrackService service) {
        return RetryingUseCases.addLayout(service);
    }

    // ── Comandos de administración ──────────────────────────────────────────────────────

    @Bean
    public VerifyCatalogService verifyCatalogService(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new VerifyCatalogService(repository, eventPublisher, clock);
    }

    @Bean
    public CorrectCatalogService correctCatalogService(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new CorrectCatalogService(repository, eventPublisher, clock);
    }

    @Bean
    public MergeVenuesUseCase mergeVenuesUseCase(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new MergeVenuesService(repository, eventPublisher, clock);
    }

    @Bean
    public MergeTracksUseCase mergeTracksUseCase(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new MergeTracksService(repository, eventPublisher, clock);
    }

    @Bean
    public MergeLayoutsUseCase mergeLayoutsUseCase(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new MergeLayoutsService(repository, eventPublisher, clock);
    }

    @Bean
    public SetDefaultLayoutUseCase setDefaultLayoutUseCase(
        VenueRepositoryPort repository,
        DomainEventPublisherPort eventPublisher,
        Clock clock
    ) {
        return new SetDefaultLayoutService(repository, eventPublisher, clock);
    }
}
