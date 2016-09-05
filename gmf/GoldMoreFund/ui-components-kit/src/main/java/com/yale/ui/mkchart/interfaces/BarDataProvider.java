package com.yale.ui.mkchart.interfaces;

import com.yale.ui.mkchart.data.BarData;

public interface BarDataProvider extends BarLineScatterCandleBubbleDataProvider {

    public BarData getBarData();
    public boolean isDrawBarShadowEnabled();
    public boolean isDrawValueAboveBarEnabled();
    public boolean isDrawHighlightArrowEnabled();
    //public boolean isDrawValuesForWholeStackEnabled();
}
