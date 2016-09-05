
package com.yale.ui.mkchart.utils;

import com.yale.ui.mkchart.data.LineData;
import com.yale.ui.mkchart.data.LineDataSet;

/**
 * Interface for providing a custom logic to where the filling line of a DataSet
 * should end. If setFillEnabled(...) is set to true.
 * 
 * @author Philipp Jahoda
 */
public interface FillFormatter {

    /**
     * Returns the vertical (y-axis) position where the filled-line of the
     * DataSet should end.
     * 
     * @param dataSet
     * @param data
     * @param chartMaxY
     * @param chartMinY
     * @return
     */
    public float getFillLinePosition(LineDataSet dataSet, LineData data, float chartMaxY,
                                     float chartMinY);
}
