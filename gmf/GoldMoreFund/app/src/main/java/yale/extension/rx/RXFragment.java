package yale.extension.rx;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.SubscriptionManager;
import com.goldmf.GMFund.controller.protocol.RXViewControllerProtocol;

import java.util.List;

import rx.Observable;
import rx.Subscription;

/**
 * Created by yale on 16/5/3.
 */
public class RXFragment extends Fragment implements RXViewControllerProtocol {
    private SubscriptionManager mSubManager = new SubscriptionManager();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSubManager.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubManager.unsubscribe();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mSubManager.unsubscribe(SubscriptionManager.LIFE_PERIOD.START_STOP);
    }

    @Override
    public final void addSubscriptionToMain(String tag, Subscription subscription) {
        if (getActivity() != null && getView() != null) {
            int key = tag == null ? subscription.hashCode() : tag.hashCode();
            mSubManager.subscribe(SubscriptionManager.LIFE_PERIOD.CREATE_DESTROY, key, subscription);
        } else {
            subscription.unsubscribe();
        }
    }

    @Override
    public final void addSubscriptionToVisible(String tag, Subscription subscription) {
        if (getUserVisibleHint() && getActivity() != null && getView() != null) {
            int key = tag == null ? subscription.hashCode() : tag.hashCode();
            mSubManager.subscribe(SubscriptionManager.LIFE_PERIOD.START_STOP, key, subscription);
        } else {
            subscription.unsubscribe();
        }
    }

    @Override
    public final boolean containSubscriptionOfMain(String tag) {
        return tag != null && mSubManager.contain(SubscriptionManager.LIFE_PERIOD.CREATE_DESTROY, tag.hashCode());

    }

    @Override
    public final boolean containSubscriptionOfVisible(String tag) {
        return tag != null && mSubManager.contain(SubscriptionManager.LIFE_PERIOD.START_STOP, tag.hashCode());
    }

    @Override
    public final void unsubscribeFromMain(String tag) {
        if (tag == null)
            return;

        int key = tag.hashCode();
        mSubManager.unsubscribe(SubscriptionManager.LIFE_PERIOD.CREATE_DESTROY, key);
    }

    @Override
    public final void unsubscribeFromVisible(String tag) {
        if (tag == null)
            return;
        int key = tag.hashCode();
        mSubManager.unsubscribe(SubscriptionManager.LIFE_PERIOD.START_STOP, key);
    }

    public void log(String format, Object... args) {
        Log.e(getClass().getSimpleName(), String.format(format, args));
    }

    public final <T> ConsumeEventChain<T> consumeEvent(Observable<T> observable) {
        return new ConsumeEventChain<T>(this).setObservable(observable);
    }

    public final <T extends MResults.MResultsInfo> ConsumeEventChainMR<T> consumeEventMR(Observable<T> observable) {
        return new ConsumeEventChainMR<T>(this).setObservable(observable);
    }

    public final <T extends List<MResults.MResultsInfo>> ConsumeEventChainMRList<T> consumeEventMRList(Observable<T> observable) {
        return new ConsumeEventChainMRList<T>(this).setObservable(observable);
    }
}
