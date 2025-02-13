/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.sample.viewmodel.mcumgr;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.runtime.mcumgr.McuMgrTransport;
import io.runtime.mcumgr.ble.McuMgrBleTransport;
import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.managers.FsManager;
import io.runtime.mcumgr.sample.viewmodel.SingleLiveEvent;
import io.runtime.mcumgr.transfer.DownloadCallback;
import io.runtime.mcumgr.transfer.TransferController;
import no.nordicsemi.android.ble.ConnectionPriorityRequest;

@SuppressWarnings("unused")
public class FilesDownloadViewModel extends McuMgrViewModel implements DownloadCallback {
    private final FsManager manager;
    private TransferController controller;

    private final MutableLiveData<Integer> progressLiveData = new MutableLiveData<>();
    private final MutableLiveData<byte[]> responseLiveData = new MutableLiveData<>();
    private final MutableLiveData<McuMgrException> errorLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> cancelledEvent = new SingleLiveEvent<>();

    @Inject
    FilesDownloadViewModel(final FsManager manager,
                           @Named("busy") final MutableLiveData<Boolean> state) {
        super(state);
        this.manager = manager;
    }

    @NonNull
    public LiveData<Integer> getProgress() {
        return progressLiveData;
    }

    @NonNull
    public LiveData<byte[]> getResponse() {
        return responseLiveData;
    }

    @NonNull
    public LiveData<McuMgrException> getError() {
        return errorLiveData;
    }

    @NonNull
    public LiveData<Void> getCancelledEvent() {
        return cancelledEvent;
    }

    public void download(final String path) {
        if (controller != null) {
            return;
        }
        setBusy();
        final McuMgrTransport transport = manager.getTransporter();
        if (transport instanceof McuMgrBleTransport) {
            ((McuMgrBleTransport) transport).requestConnPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH);
        }
        controller = manager.fileDownload(path, this);
    }

    public void pause() {
        final TransferController controller = this.controller;
        if (controller != null) {
            controller.pause();
            setReady();
        }
    }

    public void resume() {
        final TransferController controller = this.controller;
        if (controller != null) {
            setBusy();
            controller.resume();
        }
    }

    public void cancel() {
        final TransferController controller = this.controller;
        if (controller != null) {
            controller.cancel();
        }
    }

    @Override
    public void onDownloadProgressChanged(final int current, final int total, final long timestamp) {
        // Convert to percent
        progressLiveData.postValue((int) (current * 100.f / total));
    }

    @Override
    public void onDownloadFailed(@NonNull final McuMgrException error) {
        controller = null;
        progressLiveData.postValue(0);
        errorLiveData.postValue(error);
        postReady();
    }

    @Override
    public void onDownloadCanceled() {
        controller = null;
        progressLiveData.postValue(0);
        cancelledEvent.post();
        postReady();
    }

    @Override
    public void onDownloadCompleted(@NonNull final byte[] data) {
        controller = null;
        progressLiveData.postValue(0);
        responseLiveData.postValue(data);
        postReady();
    }
}
