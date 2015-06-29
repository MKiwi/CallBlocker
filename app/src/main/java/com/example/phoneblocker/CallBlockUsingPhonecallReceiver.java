package com.example.phoneblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by Marek on 12/19/2014.
 */
public class CallBlockUsingPhonecallReceiver extends PhonecallReceiver implements CallBlockCore{


    //TODO SuperService je to iste co context!

    private Boolean BlockingHidden;
    private CallBlockService SuperService;
    private Context context;
    private boolean BlockingEnabled;
    private TelephonyManager TelMan;
    private HashSet<String> NumbersDatabase;
    private android.media.AudioManager AudioManager;
    private android.os.Vibrator Vibrator;
    int PreviousRingMode ;

    public CallBlockUsingPhonecallReceiver(HashSet<String> data, Context ctx){
        BlockingHidden = false;
        context = ctx;
        BlockingEnabled = false;
        SetNumberDatabase(data);
        Initialize();
    }
    private void Initialize(){
        TelMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        AudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        setVibrator(this.Vibrator);
        PreviousRingMode = AudioManager.getRingerMode();
        Log.d("CallBlockerService, Initialize: ", "method finished.");
    }
    protected void onIncomingCallStarted(Context ctx, String number, Date start){
        Vibrator.cancel();
        PreviousRingMode = AudioManager.getRingerMode();
        //Log.d("onReceive, previous ring mode: ", ""+PreviousRingMode);
        AudioManager.setStreamMute(AudioManager.STREAM_RING, true);
        Vibrator.cancel();
        AudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        Vibrator.cancel();
        if (number == null){
            HiddenNumberCalling();
        } else {
            if (NumbersDatabase.contains(number)) EndCall();
        }
        Vibrator.cancel();
        AudioManager.setRingerMode(PreviousRingMode);
        AudioManager.setStreamMute(AudioManager.STREAM_RING, false);
        Vibrator.cancel();
        //Log.d("onReceive, ring mode set to: ", "" + AudioManager.getMode());
    }
    private void HiddenNumberCalling(){
        if(BlockingHidden)EndCall();
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

    @Override
    public boolean StartBlocking() {
        if (BlockingEnabled) {
            Log.d("CallBlockCore, StartBlocking: ", "somebody trying to register registered CallReceiver");
            return false;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        context.registerReceiver(this, filter);
        BlockingEnabled = true;
        Log.d("CallBlockCore, StartBlocking: ", "incoming call blocking enabled.");
        return true;
    }

    @Override
    public boolean StopBlocking() {
        if(!BlockingEnabled){
            Log.d("CallBlockerService, StopBlocking: ", "somebody trying to unregister unregistered CallReceiver.");
            return false;
        }
        context.unregisterReceiver(this);
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
        //TODO
        return "";
    }

    @Override
    public boolean IsBlocking() {
        return BlockingEnabled;
    }
}
