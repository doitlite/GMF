package com.goldmf.GMFund.controller.protocol;

import rx.Subscription;

/**
 * Created by yale on 16/4/6.
 */
public interface RXViewControllerProtocol {
    void addSubscriptionToMain(String tag, Subscription subscription);

    void addSubscriptionToVisible(String tag, Subscription subscription);

    void unsubscribeFromMain(String tag);

    void unsubscribeFromVisible(String tag);

    boolean containSubscriptionOfMain(String tag);

    boolean containSubscriptionOfVisible(String tag);
}
