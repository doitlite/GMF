package yale.extension.common.function;

import java.util.concurrent.Callable;

import rx.functions.Function;

/**
 * Created by yale on 16/4/21.
 */
public interface SafeFunc0<R> extends Function, Callable<R> {
    @Override
    R call() throws Exception;
}
