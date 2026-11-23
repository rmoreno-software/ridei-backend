package com.ridei.sessions.application.port.in;

import com.ridei.sessions.application.ManualTimesImportPreview;

/** Interpretar las notas pegadas y buscar sus circuitos en el catálogo, sin guardar (22C). */
public interface PreviewManualTimesImportUseCase {
    ManualTimesImportPreview preview(String notes);
}
