package yale.extension.common.function;

import rx.functions.Action;
import rx.functions.Action0;

/**
 * Created by yale on 16/4/21.
 */
public interface SafeAction0 extends Action {
    void call() throws Exception;
}
