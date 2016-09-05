package yale.extension.common;

/**
 * Created by yale on 15/10/5.
 */
public class PreCondition {
    private PreCondition() {
    }

    public static void checkNotNull(Object... args) {
        for (Object arg : args) {
            if (arg == null)
                throw new AssertionError("argument must not be null");
        }
    }
}
