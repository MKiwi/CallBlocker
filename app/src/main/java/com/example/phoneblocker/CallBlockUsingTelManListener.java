package com.example.phoneblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * Created by Marek on 12/19/2014.
 */
public class CallBlockUsingTelManListener implements CallBlockCore {

    //TODO SuperService je to iste co context!

    private Boolean BlockingHidden;
    private Context context;
    private boolean BlockingEnabled;
    private TelephonyManager TelMan;
    private android.media.AudioManager AudioManager;
    private android.os.Vibrator Vibrator;
    int PreviousRingMode ;
    private CallBlockService SuperService;
    private HashSet<String> NumbersDatabase;
    private BroadcastReceiver CallReceiver;
    private String Number;

    public CallBlockUsingTelManListener(HashSet<String> data, Context ctx){
        BlockingHidden = false;
        context = ctx;
        BlockingEnabled = false;
        Number = "";
        SetNumberDatabase(data);
        Initialize();
        MakeCallReceiver();
    }
    private void Initialize(){
        TelMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        AudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        PreviousRingMode = AudioManager.getRingerMode();
        Log.d("CallBlockerService, Initialize: ", "method finished.");
    }
    private void HiddenNumberCalling(String number){
        if(BlockingHidden){EndCall(); Number = number; Inform();}
    }
    private void EndCall() {
        Vibrator.cancel();
        try {
            Class c = Class.forName(TelMan.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            com.android.internal.telephony.ITelephony telephonyService = (ITelephony) m.invoke(TelMan);
            telephonyService.endCall();
            Log.d("CallBlockerService, EndCall: ","incoming call successfully hang up!");
        } catch (Exception e) {
            Log.e("CallBlockerService, EndCall method exception: ", e.getMessage());
            Log.e("CallBlockerService, EndCall method exception calss state details: ","");
            Log.e("BlockingEnabled setting: ", ""+BlockingEnabled);
            e.printStackTrace();
        }
    }
    private void Inform(){
        SuperService.IncomingCall(this);
    }
    private void MakeCallReceiver(){
        if(CallReceiver != null)Log.w("CallBlockerService, MakeCallReceiver warning: ", "CallReceiver variable is currently initialized");
        CallReceiver = new BroadcastReceiver() {
            // TODO !!! OVERIT CI SA VYKONA BLOK NAOZAJ LEN KED PRICHADZA HOVOR (CI SA NEVYKONAVA AJ PRI ODCHADZAJUCOM HOVORE) !!!
            // TODO !!! TENTO KOD JE CUDNY, (ALE FUNGUJE (which is nice..)) ZVAZIT JEHO KOMPLETNE PREROBENIE PODLA stackoverflow.txt

            @Override
            public void onReceive(Context context, Intent intent) {
                Vibrator.cancel();
                PreviousRingMode = AudioManager.getRingerMode();
                //Log.d("onReceive, previous ring mode: ", ""+PreviousRingMode);
                AudioManager.setStreamMute(AudioManager.STREAM_RING, true);
                AudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                Vibrator.cancel();
                TelMan.listen(PhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                Vibrator.cancel();
                AudioManager.setRingerMode(PreviousRingMode);
                AudioManager.setStreamMute(AudioManager.STREAM_RING, false);
                Vibrator.cancel();
                //Log.d("onReceive, ring mode set to: ", "" + AudioManager.getMode());
            }
            android.telephony.PhoneStateListener PhoneStateListener = new android.telephony.PhoneStateListener() {
                public void onCallStateChanged(int state, String IncomingNumber) {
                    Vibrator.cancel();
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (IncomingNumber == null){
                            HiddenNumberCalling(IncomingNumber);
                        } else {
                            if (NumbersDatabase.contains(IncomingNumber)) {
                                EndCall();
                                Number = IncomingNumber;
                                Inform();
                            }
                        }
                        AudioManager.setRingerMode(PreviousRingMode);
                        AudioManager.setStreamMute(AudioManager.STREAM_RING,false);
                        //Log.d("onCallStateChanged, ring mode set to: ", ""+AudioManager.getMode());
                    }
                }
            };
        };
        Log.d("CallBlockerService, MakeCallReceiver: ", " method finished.");
    }

    @Override
    public boolean StartBlocking() {
        if (BlockingEnabled) {
            Log.d("CallBlockerService, StartBlocking: ", "somebody trying to register registered CallReceiver");
            return true;
        }
        if(CallReceiver == null) {
            Log.w("CallBlockerService, StartBlocking: ", "somebody trying to start blocking on non-initialised CallReceiver. Returning false!");
            return false;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        context.registerReceiver(CallReceiver, filter);
        BlockingEnabled = true;
        Log.d("CallBlockerService, StartBlocking: ", "incoming call blocking enabled.");
        return true;
    }

    @Override
    public boolean StopBlocking() {
        if(!BlockingEnabled){
            Log.d("CallBlockerService, StopBlocking: ", "somebody trying to unregister unregistered CallReceiver.");
            return true;
        }
        if(CallReceiver == null){
            Log.w("CallBlockerService, StopBlocking: ", "somebody trying to stop blocking on non-initialised CallReceiver. Returning false!");
            return false;
        }
        context.unregisterReceiver(CallReceiver);
        BlockingEnabled = false;
        Log.d("CallBlockerService, StopBlocking: ", "incoming call blocking disabled.");
        return true;
    }

    @Override
    public void SetNumberDatabase(HashSet<String> data) {
        NumbersDatabase = data;
    }

    @Override
    public void SetHiddenNumberBLocking(Boolean HiddenBLocking) {
            BlockingHidden = HiddenBLocking;
    }

    @Override
    public void SetSuperClass(CallBlockService service) {
             SuperService = service;
    }

    @Override
    public String getNumber() {
        return Number;
    }

    @Override
    public boolean IsBlocking() {
        return BlockingEnabled;
    }
}
