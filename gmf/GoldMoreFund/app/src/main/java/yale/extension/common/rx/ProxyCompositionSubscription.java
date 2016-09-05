package yale.extension.common.rx;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by yale on 15/10/24.
 */
public class ProxyCompositionSubscription {
    private CompositeSubscription mSubscriptions;

    public static ProxyCompositionSubscription create() {
        return new ProxyCompositionSubscription();
    }

    public ProxyCompositionSubscription() {
        mSubscriptions = new CompositeSubscription();
    }

    public void add(Subscription subscription) {
        if (mSubscriptions != null) {
            mSubscriptions.add(subscription);
        }
    }

    public void reset() {
        if (mSubscriptions != null)
            mSubscriptions.unsubscribe();
        mSubscriptions = new CompositeSubscription();
    }

    public void close() {
        if (mSubscriptions != null)
            mSubscriptions.unsubscribe();
        mSubscriptions = null;
    }

}
