package yale.extension.common;

/**
 * Created by yale on 16/2/25.
 */
public class RangeD {
    public final double min;
    public final double max;

    public RangeD(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public boolean contain(double value) {
        return value >= min && value <= max;
    }
}
