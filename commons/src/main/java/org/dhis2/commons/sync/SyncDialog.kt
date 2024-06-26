package org.dhis2.commons.sync

import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.components.ComponentProvider

class SyncDialog(
    val activity: FragmentActivity,
    val recordUid: String,
    val syncContext: SyncContext,
    private val dismissListener: OnDismissListener? = null,
    private val onSyncNavigationListener: OnSyncNavigationListener? = null,
    private val onNoConnectionListener: OnNoConnectionListener? = null,
) {
    fun show() {
        (activity.applicationContext as? ComponentProvider)
            ?.syncComponentProvider
            ?.showSyncStatusDialog(
                activity,
                syncContext,
                dismissListener,
                onSyncNavigationListener,
                onNoConnectionListener,
            )
    }
}
