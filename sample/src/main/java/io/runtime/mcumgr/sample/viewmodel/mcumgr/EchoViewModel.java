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
import io.runtime.mcumgr.McuMgrCallback;
import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.managers.DefaultManager;
import io.runtime.mcumgr.response.dflt.McuMgrEchoResponse;

public class EchoViewModel extends McuMgrViewModel {
    private final DefaultManager manager;

    private final MutableLiveData<String> requestLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> responseLiveData = new MutableLiveData<>();
    private final MutableLiveData<McuMgrException> errorLiveData = new MutableLiveData<>();

    @Inject
    EchoViewModel(final DefaultManager manager,
                  @Named("busy") final MutableLiveData<Boolean> state) {
        super(state);
        this.manager = manager;
    }

    @NonNull
    public LiveData<String> getRequest() {
        return requestLiveData;
    }

    @NonNull
    public LiveData<String> getResponse() {
        return responseLiveData;
    }

    @NonNull
    public LiveData<McuMgrException> getError() {
        return errorLiveData;
    }

    public void echo(final String echo) {
        setBusy();
        requestLiveData.postValue(echo);
        manager.echo(echo, new McuMgrCallback<McuMgrEchoResponse>() {
            @Override
            public void onResponse(@NonNull final McuMgrEchoResponse response) {
                responseLiveData.postValue(response.r);
                postReady();
            }

            @Override
            public void onError(@NonNull final McuMgrException error) {
                errorLiveData.postValue(error);
                postReady();
            }
        });
    }
}
