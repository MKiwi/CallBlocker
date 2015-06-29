package com.example.phoneblocker;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

/**
 * Created by Marek on 4/30/2015.
 */
public interface BindableWithCommunicator {
    public boolean doBind(ServiceConnection con);
    public void doUnbind(ServiceConnection con);
}
