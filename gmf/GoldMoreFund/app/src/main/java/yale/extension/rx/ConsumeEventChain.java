package yale.extension.rx;

import com.goldmf.GMFund.controller.protocol.RXViewControllerProtocol;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import yale.extension.common.function.SafeAction0;
import yale.extension.common.function.SafeAction1;

import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;

/**
 * Created by yale on 16/4/6.
 */
public class ConsumeEventChain<T> {
    public enum POLICY {
        REPLACE,
        IGNORED
    }

    private WeakReference<RXViewControllerProtocol> mVCRef = new WeakReference<>(null);
    protected Observable<T> mObservable;
    protected SafeAction0 mOnConsumed;
    protected SafeAction1<T> mOnStart;
    protected SafeAction1<T> mOnFinish;
    protected SafeAction0 mOnComplete;
    protected boolean mIsAddToMain = true;
    protected boolean mIsAddToVisible = false;
    protected String mTag;
    protected POLICY mPolicy = POLICY.REPLACE;

    public ConsumeEventChain(RXViewControllerProtocol vc) {
        mVCRef = new WeakReference<>(vc);
    }

    public ConsumeEventChain<T> setObservable(Observable<T> observable) {
        mObservable = observable;
        return this;
    }

    public ConsumeEventChain<T> setTag(String tag) {
        mTag = tag;
        return this;
    }

    public ConsumeEventChain<T> setPolicy(POLICY policy) {
        mPolicy = policy == null ? POLICY.REPLACE : policy;
        return this;
    }

    public ConsumeEventChain<T> addToMain() {
        mIsAddToMain = true;
        mIsAddToVisible = false;
        return this;
    }

    public ConsumeEventChain<T> addToVisible() {
        mIsAddToMain = false;
        mIsAddToVisible = true;
        return this;
    }


    public ConsumeEventChain<T> onConsumed(SafeAction0 operation) {
        mOnConsumed = operation;
        return this;
    }

    public ConsumeEventChain<T> onNextStart(SafeAction1<T> operation) {
        mOnStart = operation;
        return this;
    }

    public ConsumeEventChain<T> onNextFinish(SafeAction1<T> operation) {
        mOnFinish = operation;
        return this;
    }

    public ConsumeEventChain<T> onComplete(SafeAction0 mOnComplete) {
        this.mOnComplete = mOnComplete;
        return this;
    }

    public SafeAction0 getOnConsumed() {
        return mOnConsumed;
    }

    public SafeAction1<T> getOnNextStart() {
        return mOnStart;
    }

    public SafeAction1<T> getOnNextFinish() {
        return mOnFinish;
    }

    public SafeAction0 getOnComplete() {
        return mOnComplete;
    }

    public void done() {
        if (mObservable == null) return;

        opt(getOnConsumed()).consume(it -> safeCall(() -> it.call()));
        if (mVCRef.get() != null) {
            if (mPolicy == POLICY.IGNORED) {
                RXViewControllerProtocol vc = mVCRef.get();
                if (mIsAddToMain && vc.containSubscriptionOfMain(mTag)) return;
                if (mIsAddToVisible && vc.containSubscriptionOfVisible(mTag)) return;
            }

            Subscription sub = mObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(response -> {
                        opt(getOnNextStart()).consume(it -> safeCall(() -> it.call(response)));
                        opt(getOnNextFinish()).consume(it -> safeCall(() -> it.call(response)));
                    })
                    .doOnCompleted(() -> {
                        opt(getOnComplete()).consume(it -> safeCall(() -> it.call()));
                    })
                    .subscribe();


            if (mIsAddToMain && mVCRef.get() != null) {
                mVCRef.get().addSubscriptionToMain(mTag, sub);
            }
            if (mIsAddToVisible && mVCRef.get() != null) {
                mVCRef.get().addSubscriptionToVisible(mTag, sub);
            }
        }
    }
}
