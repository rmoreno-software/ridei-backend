package com.ridei.circuits.domain.model;

import java.util.List;

/** Resultado de fusionar un track en otro: layouts movidos y layouts fusionados con su equivalente. */
record TrackAbsorption(List<LayoutId> movedLayouts, List<MergedLayout> mergedLayouts) {

    record MergedLayout(LayoutId source, LayoutId target) {
    }
}
