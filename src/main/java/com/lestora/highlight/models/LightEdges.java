package com.lestora.highlight.models;

import com.lestora.highlight.helpers.HighlightEntry;

import java.util.List;

public class LightEdges {
    public final List<HighlightEntry> LightLevel1;
    public final List<HighlightEntry> LightLevel0;

    public LightEdges(List<HighlightEntry> lightLevel1, List<HighlightEntry> lightLevel0) {
        this.LightLevel1 = lightLevel1;
        this.LightLevel0 = lightLevel0;
    }
}