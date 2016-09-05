package yale.extension.rx;

import android.support.v7.app.AppCompatActivity;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.SubscriptionManager;
import com.goldmf.GMFund.controller.SubscriptionManager.LIFE_PERIOD;
import com.goldmf.GMFund.controller.protocol.RXViewControllerProtocol;

import java.util.List;

import rx.Observable;
import rx.Subscription;

/**
 * Created by yale on 16/5/3.
 */
public class RXActivity extends AppCompatActivity implements RXViewControllerProtocol {
    private SubscriptionManager mSubManager = new SubscriptionManager();

    @Override
    protected void onStart() {
        super.onStart();
        mSubManager.unsubscribe(LIFE_PERIOD.START_STOP);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSubManager.unsubscribe(LIFE_PERIOD.START_STOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubManager.unsubscribe();
    }

//    @Override
//    public final void addSubscriptionToMain(Subscription subscription) {
//        addSubscriptionToMain(null, subscription);
//    }

    @Override
    public final void addSubscriptionToMain(String tag, Subscription subscription) {
        int key = tag == null ? subscription.hashCode() : tag.hashCode();
        mSubManager.subscribe(LIFE_PERIOD.CREATE_DESTROY, key, subscription);
    }

    @Override
    public final void addSubscriptionToVisible(String tag, Subscription subscription) {
        int key = tag == null ? subscription.hashCode() : tag.hashCode();
        mSubManager.subscribe(LIFE_PERIOD.START_STOP, key, subscription);
    }

    @Override
    public final void unsubscribeFromMain(String tag) {
        if (tag == null)
            return;
        int key = tag.hashCode();
        mSubManager.unsubscribe(LIFE_PERIOD.CREATE_DESTROY, key);
    }

    @Override
    public final void unsubscribeFromVisible(String tag) {
        if (tag == null)
            return;
        int key = tag.hashCode();
        mSubManager.unsubscribe(LIFE_PERIOD.START_STOP, key);
    }

    @Override
    public boolean containSubscriptionOfMain(String tag) {
        return tag != null && mSubManager.contain(LIFE_PERIOD.CREATE_DESTROY, tag.hashCode());
    }

    @Override
    public boolean containSubscriptionOfVisible(String tag) {
        return tag != null && mSubManager.contain(LIFE_PERIOD.START_STOP, tag.hashCode());
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
