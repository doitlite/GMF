package com.yale.ui.mkchart.interfaces;

import com.yale.ui.mkchart.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    public CandleData getCandleData();
}
