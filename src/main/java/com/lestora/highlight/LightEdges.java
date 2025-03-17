package com.lestora.highlight;

import java.util.List;

class LightEdges {
    public final List<HighlightEntry> LightLevel1;
    public final List<HighlightEntry> LightLevel0;

    public LightEdges(List<HighlightEntry> lightLevel1, List<HighlightEntry> lightLevel0) {
        this.LightLevel1 = lightLevel1;
        this.LightLevel0 = lightLevel0;
    }
}