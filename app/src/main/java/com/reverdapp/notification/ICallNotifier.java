package com.reverdapp.notification;

/**
 * Created by wojci on 8/19/15.
 */
public interface ICallNotifier {
    // Received a call and it was blocked.
    // @param phone The number of the blocked phone.
    // @param statText Text which describes call statistics
    //                 (for example number of calls during the last hour)
    void onBlockedCall(final String phone, final String statText);

    // Received a call and it was received because of blacklist.
    void onWhiteListedCall(final String phone);

    // Received a call that was not in any list.
    void onCall(final String phone);

    // Current notification was cancelled.
    void onNotificationCancel();

    // Current notification was activated.
    void onNotificationActivated();
}
