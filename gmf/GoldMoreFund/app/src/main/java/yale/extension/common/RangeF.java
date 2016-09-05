package yale.extension.common;

/**
 * Created by yale on 16/2/25.
 */
public class RangeF {
    public final float min;
    public final float max;

    public RangeF(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public boolean contain(float value) {
        return value >= min && value <= max;
    }
}
