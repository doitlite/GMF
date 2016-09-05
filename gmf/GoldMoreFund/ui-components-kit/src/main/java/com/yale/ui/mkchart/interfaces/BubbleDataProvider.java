package com.yale.ui.mkchart.interfaces;

import com.yale.ui.mkchart.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    public BubbleData getBubbleData();
}
