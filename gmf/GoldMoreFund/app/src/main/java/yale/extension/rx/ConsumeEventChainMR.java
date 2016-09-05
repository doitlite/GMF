package yale.extension.rx;

import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.controller.protocol.RXViewControllerProtocol;

import rx.Observable;
import yale.extension.common.function.SafeAction0;
import yale.extension.common.function.SafeAction1;

import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;

/**
 * Created by yale on 16/4/6.
 */
public class ConsumeEventChainMR<T extends MResultsInfo> extends ConsumeEventChain<T> {
    protected SafeAction1<T> mOnSuccess;
    protected SafeAction1<T> mOnFail;

    public ConsumeEventChainMR(RXViewControllerProtocol vc) {
        super(vc);
    }

    @Override
    public ConsumeEventChainMR<T> setTag(String tag) {
        super.setTag(tag);
        return this;
    }

    @Override
    public ConsumeEventChainMR<T> setPolicy(POLICY policy) {
        super.setPolicy(policy);
        return this;
    }

    @Override
    public ConsumeEventChainMR<T> addToMain() {
        super.addToMain();
        return this;
    }

    @Override
    public ConsumeEventChainMR<T> addToVisible() {
        super.addToVisible();
        return this;
    }

    @Override
    public ConsumeEventChainMR<T> setObservable(Observable<T> observable) {
        super.setObservable(observable);
        return this;
    }

    public ConsumeEventChainMR<T> onConsumed(SafeAction0 operation) {
        mOnConsumed = operation;
        return this;
    }

    @Override
    public ConsumeEventChainMR<T> onNextStart(SafeAction1<T> operation) {
        super.onNextStart(operation);
        return this;
    }

    public ConsumeEventChainMR<T> onNextSuccess(SafeAction1<T> operation) {
        mOnSuccess = operation;
        return this;
    }

    public ConsumeEventChainMR<T> onNextFail(SafeAction1<T> operation) {
        mOnFail = operation;
        return this;
    }

    @Override
    public ConsumeEventChain<T> onNextFinish(SafeAction1<T> operation) {
        super.onNextFinish(operation);
        return this;
    }

    public SafeAction0 getOnConsumed() {
        return mOnConsumed;
    }

    public SafeAction1<T> getOnNextStart() {
        return response -> {
            safeCall(() -> {
                SafeAction1<T> action = this.mOnStart;
                if (action != null) {
                    action.call(response);
                }
            });
            if (isSuccess(response)) {
                safeCall(() -> {
                    SafeAction1<T> action = getOnNextSuccess();
                    if (action != null) {
                        action.call(response);
                    }
                });
            } else {
                safeCall(() -> {
                    SafeAction1<T> action = getOnNextFail();
                    if (action != null) {
                        action.call(response);
                    }
                });
            }
        };
    }

    public SafeAction1<T> getOnNextSuccess() {
        return mOnSuccess;
    }

    public SafeAction1<T> getOnNextFail() {
        return mOnFail;
    }
}
