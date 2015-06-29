package com.example.phoneblocker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.HashSet;

/**
 * Created by Marek on 12/20/2014.
 * This class is designed for communication with CallBlockService service.
 * This communication interface is based on Binder and currently is offering
 * only one-way communication from Application to service.
 */
public class Communicator {

        private BindableWithCommunicator mainActivity;
        private CallBlockService CallBlockService;
        private Boolean Bound;
        private ServiceConnection Connection;
        private int Stats = -1;
    /**@param context -context of Application which wants to interact with service.
     * */
    public Communicator(Context context){
        mainActivity = (BindableWithCommunicator) context;
        Bound = false;
    }
    /**@return true is Communicator is bound to CallBlockService class, otherwise false.
     * */
    public boolean isBounded(){
        //Log.d("Communicator isBounded: ","returning: "+Bound);
        return Bound;
    }
    /**Removes number from CallBlockService BlockList database*/
    public void RemoveNumberFromBlockList(final String number){
       if(Bound) {
           CallBlockService.RemoveNumberFromBlockList(number);
       }else {
           Connection = new ServiceConnection() {
               @Override
               public void onServiceConnected(ComponentName className, IBinder service) {
                   CallBlockService.CallBlockServiceBinder binder = (CallBlockService.CallBlockServiceBinder) service;
                   CallBlockService = binder.getService();
                   Bound = true;
                   if (CallBlockService != null) {
                       CallBlockService.RemoveNumberFromBlockList(number);
                   } else {
                       Log.e("Communicator error: ", "RemoveNumberFromBlockList: CallBlockService was null, something failed!");
                       mainActivity.doUnbind(Connection);
                       Bound = false;
                   }
               }

               @Override
               public void onServiceDisconnected(ComponentName arg0) {
                   Bound = false;
               }
           };
           mainActivity.doBind(Connection);
       }
    }
    /**If Communicator is bound to CallBlockService this method unbind from service.
     * */
    public void Disconnect(){
        if(Bound) {
           mainActivity.doUnbind(Connection);
            Bound = false;
            Log.d("Communicator Disconnect: ","unbinding from CallBlockService.");
        }
    }
    /**Adds number to BlockList of CallBlockService*/
    public void AddNumberToBlockList(final String number){
        if(Bound) {
            CallBlockService.AddNumberToBlockList(number);
        }else {
            Connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    CallBlockService.CallBlockServiceBinder binder = (CallBlockService.CallBlockServiceBinder) service;
                    CallBlockService = binder.getService();
                    Bound = true;
                    if (CallBlockService != null) {
                        Log.e("A", "asd"+number);
                        CallBlockService.AddNumberToBlockList(number);
                    } else {
                        Log.e("Communicator error: ", "AddNumberToBlockList: CallBlockService was null, something failed!");
                        mainActivity.doUnbind(Connection);
                        Bound = false;
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    Bound = false;
                }
            };
            mainActivity.doBind(Connection);
        }
    }
    /**Sets new BlockList to CallBlockService*/
    public void SetNumbersBlockList(final HashSet<String> data) {
        if(Bound) {
            CallBlockService.SetNumbersBlockList(data);
        }else {
            Connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    CallBlockService.CallBlockServiceBinder binder = (CallBlockService.CallBlockServiceBinder) service;
                    CallBlockService = binder.getService();
                    Bound = true;
                    if (CallBlockService != null) {
                        CallBlockService.SetNumbersBlockList(data);
                    } else {
                        Log.e("Communicator error: ", "SetNumbersBlockList: CallBlockService was null,  something failed!");
                        mainActivity.doUnbind(Connection);
                        Bound = false;
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    Bound = false;
                }
            };
            mainActivity.doBind(Connection);
        }
    }
    /**Sends object to CallBlockService*/
    public void Other(final Object o){
        if(Bound) {
            CallBlockService.Other(o);
        }else {
            Connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    CallBlockService.CallBlockServiceBinder binder = (CallBlockService.CallBlockServiceBinder) service;
                    CallBlockService = binder.getService();
                    Bound = true;
                    if (CallBlockService != null) {
                        CallBlockService.Other(o);
                    } else {
                        Log.e("Communicator error: ", "Other: CallBlockService was null,  something failed!");
                        mainActivity.doUnbind(Connection);
                        Bound = false;
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    Bound = false;
                }
            };
            mainActivity.doBind(Connection);
        }
    }

    public class StatObject{
        boolean valueSet = false;
        int stat = -47;
    }
}
