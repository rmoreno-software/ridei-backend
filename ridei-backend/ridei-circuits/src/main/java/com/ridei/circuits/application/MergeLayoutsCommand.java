package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.LayoutId;

public record MergeLayoutsCommand(
    LayoutId sourceId,
    LayoutId targetId
) {
    public MergeLayoutsCommand {
        if (sourceId == null || targetId == null) {
            throw new IllegalArgumentException("Source and target are required");
        }
    }
}
