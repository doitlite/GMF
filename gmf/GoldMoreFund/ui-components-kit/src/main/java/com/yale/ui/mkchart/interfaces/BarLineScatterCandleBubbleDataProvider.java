package com.yale.ui.mkchart.interfaces;

import com.yale.ui.mkchart.components.YAxis;
import com.yale.ui.mkchart.data.BarLineScatterCandleData;
import com.yale.ui.mkchart.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    public Transformer getTransformer(YAxis.AxisDependency axis);
    public int getMaxVisibleCount();
    public boolean isInverted(YAxis.AxisDependency axis);
    
    public int getLowestVisibleXIndex();
    public int getHighestVisibleXIndex();

    public BarLineScatterCandleData getData();
}
