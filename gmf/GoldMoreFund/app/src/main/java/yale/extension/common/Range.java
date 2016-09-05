package yale.extension.common;

/**
 * Created by yale on 16/2/25.
 */
public class Range {
    public final int min;
    public final int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean contain(int value) {
        return value >= min && value <= max;
    }
}
