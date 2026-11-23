package com.ridei.sessions.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.ridei.sessions.application.LapAnalysisService;
import com.ridei.sessions.application.ListSessionsService;
import com.ridei.sessions.application.ManageSessionService;
import com.ridei.sessions.application.ManualLapTimesService;
import com.ridei.sessions.application.PersonalRecordsService;
import com.ridei.sessions.application.PreviewManualTimesImportService;
import com.ridei.sessions.application.RecordManualLapTimesService;
import com.ridei.sessions.application.RecordSessionService;
import com.ridei.sessions.application.port.in.GetPersonalRecordsUseCase;
import com.ridei.sessions.application.port.in.PreviewManualTimesImportUseCase;
import com.ridei.sessions.application.port.in.RecordSessionUseCase;
import com.ridei.sessions.domain.port.out.GaragePort;
import com.ridei.sessions.domain.port.out.LayoutCatalogPort;
import com.ridei.sessions.domain.port.out.ManualLapTimeRepositoryPort;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.port.out.TelemetryRepositoryPort;
import com.ridei.sessions.domain.port.out.VenueCatalogPort;
import com.ridei.sessions.infrastructure.adapter.out.circuits.CircuitsCatalogAdapter;
import com.ridei.sessions.infrastructure.adapter.out.garage.GarageAdapter;

/**
 * Cableado manual de los casos de uso. Algunos servicios implementan varios casos de uso
 * relacionados (consultas de la misma sesión); se exponen como un bean cada uno y el controlador
 * depende solo de las interfaces {@code *UseCase}.
 */
@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /** {@code LayoutCatalogPort} (con geometría, para cronometrar) + {@code VenueCatalogPort} (sin ella, tiempos manuales). */
    @Bean
    public CircuitsCatalogAdapter circuitsCatalogAdapter(ClientsProperties properties) {
        return new CircuitsCatalogAdapter(restClient(properties.circuitsBaseUrl(), properties));
    }

    @Bean
    public GaragePort garagePort(ClientsProperties properties) {
        return new GarageAdapter(restClient(properties.garageBaseUrl(), properties));
    }

    @Bean
    public RecordSessionUseCase recordSessionUseCase(
        SessionRepositoryPort sessionRepository,
        TelemetryRepositoryPort telemetryRepository,
        LayoutCatalogPort layoutCatalog,
        GaragePort garage,
        Clock clock
    ) {
        return new RecordSessionService(sessionRepository, telemetryRepository, layoutCatalog, garage, clock);
    }

    /** {@code ListSessionsUseCase} + {@code GetSessionUseCase}. */
    @Bean
    public ListSessionsService listSessionsService(SessionRepositoryPort sessionRepository) {
        return new ListSessionsService(sessionRepository);
    }

    /** {@code GetLapTraceUseCase} + {@code GetCornerAnalysisUseCase} + {@code GetReferenceLapUseCase}. */
    @Bean
    public LapAnalysisService lapAnalysisService(SessionRepositoryPort sessionRepository, TelemetryRepositoryPort telemetryRepository) {
        return new LapAnalysisService(sessionRepository, telemetryRepository);
    }

    @Bean
    public GetPersonalRecordsUseCase getPersonalRecordsUseCase(
        SessionRepositoryPort sessionRepository,
        ManualLapTimeRepositoryPort manualLapTimeRepository
    ) {
        return new PersonalRecordsService(sessionRepository, manualLapTimeRepository);
    }

    /** {@code RecordManualLapTimeUseCase} + {@code ImportManualLapTimesUseCase}. */
    @Bean
    public RecordManualLapTimesService recordManualLapTimesService(
        ManualLapTimeRepositoryPort manualLapTimeRepository,
        VenueCatalogPort venueCatalog,
        GaragePort garage,
        Clock clock
    ) {
        return new RecordManualLapTimesService(manualLapTimeRepository, venueCatalog, garage, clock);
    }

    @Bean
    public PreviewManualTimesImportUseCase previewManualTimesImportUseCase(VenueCatalogPort venueCatalog, Clock clock) {
        return new PreviewManualTimesImportService(venueCatalog, clock);
    }

    /** {@code ListManualLapTimesUseCase} + {@code GetManualLapTimeUseCase} + {@code DeleteManualLapTimeUseCase}. */
    @Bean
    public ManualLapTimesService manualLapTimesService(ManualLapTimeRepositoryPort manualLapTimeRepository) {
        return new ManualLapTimesService(manualLapTimeRepository);
    }

    /** {@code EditSessionUseCase} + {@code RecalculateSessionUseCase} + {@code DeleteSessionUseCase}. */
    @Bean
    public ManageSessionService manageSessionService(
        SessionRepositoryPort sessionRepository,
        TelemetryRepositoryPort telemetryRepository,
        LayoutCatalogPort layoutCatalog,
        Clock clock
    ) {
        return new ManageSessionService(sessionRepository, telemetryRepository, layoutCatalog, clock);
    }

    private static RestClient restClient(String baseUrl, ClientsProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
