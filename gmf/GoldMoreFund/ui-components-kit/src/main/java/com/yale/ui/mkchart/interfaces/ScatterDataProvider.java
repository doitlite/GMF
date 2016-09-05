package com.yale.ui.mkchart.interfaces;

import com.yale.ui.mkchart.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
    
}
