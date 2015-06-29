package com.example.phoneblocker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Marek Turza on 12/13/2014.
 * This class is Service running on background providing blocking incoming numbers from blocklist.
 * This class can be started only with startService(new Intent(this, CallBlockService.class)); command
 * if you want to run service.
 */
public class CallBlockService extends Service {

    /*----!!!!! THIS CODE CONTAINS TODO SECTIONS   !!!! --------- */
    //TODO ukladat vsetko do settingov aby pri boote vedel ako sa ma nastavit-TJ. UPRAVIT TRIEDU ABY SA VEDELA STARTOVAT AJ Z
    //TODO INEHO ZDROJA AKO MAIN ACTIVITY
    //TODO doimplementovat settings aj do SharedPreferencesDatabase aj do Communicator-a
    //TODO nepamatat extra ci je blokovanie zapate a ci nie ale vyuzivat BlockingStrategy.IsEnabled();

    //------------------------
    //GLOBAL VARIABLES SECTION
    private HashSet<String> NumbersDatabase;
    private CallBlockCore BlockingStrategy;
    private DatabaseObject Database;
    private final IBinder MyBinder = new CallBlockServiceBinder();
    //END OF GLOBAL VARIABLES SECTION
    //-------------------------------

    //--------------------
    //CONSTRUCTORS SECTION
    /**Constructor
     * Do not use this class by calling constructor, this class is Service.
     * For launching this Service use startService(new Intent(this, CallBlockService.class));
     * (or similar command) in your application.
     * */
    public CallBlockService(){
        super();
    }
    //END OF CONSTRUCTORS SECTION
    //---------------------------

    //---------------------
    //SUPPLEMENTARY METHODS
    /**Loads phone numbers to be blocked from source managed by NumberSavingStrategy to HashSet.
     *@return HashSet<String> of phone numbers to be blocked.
     **/
    private HashSet<String> LoadDatabase(){
        HashSet<String> data = null;
        if(Database.CheckAvailability())data = Database.GetNumbersFromDatabase();
        if(data == null) data = new HashSet<String>();
        Log.d("CallBlockerService, LoadDatabase: ", "method finished. Returning Database.");
        return data;
    }

    /**Method invoked by BlockingStrategy, notifies about number which was hang up.*/
    public void IncomingCall(CallBlockCore core){
        Log.d("CallBlockerService IncomingCall: ", "notified about hang up number.");
        Database.IncrementStats();
        Calendar calendar = Calendar.getInstance();
        String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(calendar.getTime().getTime());
        String number = core.getNumber();
        String[] array = {number, date};
        Set<String> set = new HashSet<String>(Arrays.asList(array));
        Database.AddLastBlockedNumber(set);
    }

    //-----------------------------
    //METHODS SUPPORTED FOR BINDING
    public void SetNumbersBlockList(HashSet<String> data){
        Log.d("CallBlockerService", " SetNumbersBlockList: setting numbers.");
        NumbersDatabase = data;
        BlockingStrategy.SetNumberDatabase(data);
        Database.SetNumbersToDatabase(data);
    }
    public void AddNumberToBlockList(String number){
        Log.d("CallBlockerService", " AddNumberToBlockList: adding number.");
        NumbersDatabase.add(number);
        BlockingStrategy.SetNumberDatabase(NumbersDatabase);
        Database.AddNumberToDatabase(number);
    }
    public void RemoveNumberFromBlockList(String number){
        Log.d("CallBlockerService", " RemoveNumberFromBlockList: removing number.");
        NumbersDatabase.remove(number);
        BlockingStrategy.SetNumberDatabase(NumbersDatabase);
        Database.RemoveNumberFromDatabase(number);
    }
    public void Other(Object o){
       // Log.d("CallBlockerService Other: ", "doing something.");
        //TODO
    }
    public void SetBlockingStrategy(String data){
        //TODO preco nie rovno parameter blockingstrategy??
        Boolean set = false;
        if (data != null) {
          //  Log.d("CallBlockService SetBlockingStrategy: ", "Intent parameter string content: "+data);
            //If you add new CallBlockCore strategies please add new else if branch.
            if (data.equals("PhonecallReceiver")) {
               // Log.d("CallBlockService SetBlockingStrategy: ", "creating new CallBlockUsingPhonecallReceiver strategy.");
                CallBlockCore strat = new CallBlockUsingPhonecallReceiver(NumbersDatabase, this);
                strat.SetSuperClass(this);
                BlockingStrategy = strat;
                set = true;
            }
        }
        if(!set){
            //If BlockingStrategy was yet not set and there is not information in intent about
            //required strategy, use default CallBlockUsingTelManListener strategy.
            if(BlockingStrategy == null) {
               // Log.d("CallBlockService", " SetBlockingStrategy: setting BlockingStrategy for the first time (CallBlockUsingTelManListener).");
                CallBlockCore strat = new CallBlockUsingTelManListener(NumbersDatabase, this);
                strat.SetSuperClass(this);
                BlockingStrategy = strat;
            }
        }
    }
    public int GetStats(){
        return Database.GetStats();
    }

    //---------------------
    //BINDING METHODS
        //----------------------
        //CALLBLOCKSERVICEBINDER
        class CallBlockServiceBinder extends Binder {
            public CallBlockService getService(){
                return CallBlockService.this;
            }
        }
        //END OF CALLBLOCKSERVICEBINDER SECTION
        //-------------------------------------

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("CallBlockerService", "onBind: request for binding, returning MyBinder.");
        return MyBinder;
    }
    //END OF SUPPLEMENTARY METHODS SECTION
    //------------------------------------

    //----------------------
    //SERVICE METHODS
    /**On create block
     * This method is called by system and should not be used in other way.
     * Initialize variables.
     * */
    @Override
    public void onCreate(){
        Database = new SharedPreferencesDatabase(this);
        NumbersDatabase = LoadDatabase();
        Database.setServiceStatus(true);
        super.onCreate();
        Log.d("CallBlockerService", "onCreate: method finished, variables initialized.");
    }
    /**This method offers setting a new CallBlockCore blocking strategy.
     * This change should be possible even when service is running.*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CallBlockerService", "onStartCommand: setting BlockingStrategy if necessary, starting blocking.");
        //PODLA VSETKEHO TIETO DVA RIADKY NEREBA ROBIT, NEVIEM....
        //NumbersDatabase = LoadDatabase();
        //BlockingStrategy.SetNumberDatabase(NumbersDatabase);
        String data;
        if(intent == null) {
            data = "";
        } else {
            data = intent.getDataString();
        }
        SetBlockingStrategy(data);
        BlockingStrategy.StartBlocking();
        //StartBlocking();
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
    @Override
    public void onDestroy(){
        Database.SetNumbersToDatabase(NumbersDatabase);
        Database.setServiceStatus(false);
        BlockingStrategy.StopBlocking();
        Log.d("CallBlockService", "onDestroy: service is exiting.");
        super.onDestroy();
    }
    //END OF SERVICE METHODS SECTION
    //-------------------------------------
}
