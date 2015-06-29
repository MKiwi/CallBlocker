package com.example.phoneblocker;

import android.content.Context;

import java.util.HashSet;

/**
 * Created by Marek on 12/19/2014.
 * Interface for class which wants to provide blocking number with using different system tool.
 */
public interface CallBlockCore {
    /**Starts blocking*/
    public boolean StartBlocking();
    /**Stops blocking*/
    public boolean StopBlocking();
    /**Sets database of numbers to be blocked*/
    public void SetNumberDatabase(HashSet<String> data);
    /**Sets if hidden numbers will be blocked or not*/
    public void SetHiddenNumberBLocking(Boolean HiddenBLocking);
    /**Sets super class (CallBlockService) on which this class should invoke IncomingCall method when
     * call was hang up by this class.*/
    public void SetSuperClass(CallBlockService service);
    /**Returns number of last blocked call.*/
    public String getNumber();
    /**Returns if blocking is enabled.
     * @return true if blocking is enabled
     * @return false if blocking is disabled*/
    public boolean IsBlocking();
}
