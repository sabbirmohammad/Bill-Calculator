package com.powerunit.core.update

import androidx.lifecycle.ViewModel
import com.powerunit.core.update.model.UpdateInfo
import com.powerunit.core.update.model.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    val updateManager: AppUpdateManager
) : ViewModel() {

    val updateState: StateFlow<UpdateState> = updateManager.updateState

    init {
        // Automatically check for updates on application launch
        updateManager.checkForUpdates(isManual = false)
    }

    fun startDownload(info: UpdateInfo) {
        updateManager.startDownload(info)
    }

    fun cancelDownload() {
        updateManager.cancelDownload()
    }

    fun installApk(file: File, callerContext: android.content.Context? = null) {
        updateManager.installApk(file, callerContext)
    }

    fun dismiss() {
        updateManager.dismissUpdate()
    }
}
