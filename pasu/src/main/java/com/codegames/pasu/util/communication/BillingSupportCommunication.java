package com.codegames.pasu.util.communication;


import com.codegames.pasu.util.IabResult;

public interface BillingSupportCommunication {
    void onBillingSupportResult(int response);

    void remoteExceptionHappened(IabResult result);
}