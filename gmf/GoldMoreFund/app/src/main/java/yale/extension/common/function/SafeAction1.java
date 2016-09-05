package yale.extension.common.function;

import rx.functions.Action;

/**
 * Created by yale on 16/4/21.
 */
public interface SafeAction1<T> extends Action {
    void call(T arg) throws Exception;
}
