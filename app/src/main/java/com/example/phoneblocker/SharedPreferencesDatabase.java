package com.example.phoneblocker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Marek on 12/19/2014.
 */
public class SharedPreferencesDatabase implements DatabaseObject {
    //      IMPORTANT-   KEYS    ON  WHICH   ARE VALUES  STORED:
    //PREFERENCES HAVE TO CONTAIN KEY "AVAILABLE" IF SHAREDPREFERENCES CONTAINS DATA
    //KEY "COUNT" CONTAINS COUNT OF CALL NUMBERS IN SHAREDPREFERENCES
    //NUMBERS ARE STORED WITH KEYS "NUMBERx" WHERE X IS NUMBER FROM RANGE 0 TO COUNT-1
    //KEY "STATS" CONTAINS NUMBERS OF INCOMING CALLS HANG UP BY CallBlockService

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Boolean isEmpty;

    final String AVAILABILITY = "AVAILABLE";
    final String COUNT_OF_NUM = "COUNT";
    final String STATS = "STATS";
    final String NUMBER = "NUMBER";
    final String ERROR = "error";
    final String BLOCKED_LOG = "BLOCKED";
    final String NUM_BLOCK_LOG = "LOG_NUM";
    final String SERVICE_STATUS = "SERVICE_STATUS";

    public SharedPreferencesDatabase(Context ctx){
        context = ctx;
        preferences = context.getSharedPreferences("STORE", Context.MODE_MULTI_PROCESS);
        editor = preferences.edit();
        if(preferences.contains(AVAILABILITY)){
            isEmpty=false;
        } else {
            isEmpty=true;
        }
    }
    @Override
    public void AddLastBlockedNumber(Set<String> set) {
        int COUNT = preferences.getInt(NUM_BLOCK_LOG, 0);
        editor.putStringSet(BLOCKED_LOG+COUNT, set);
        COUNT++;
        editor.putInt(NUM_BLOCK_LOG, COUNT);
        editor.commit();
    }

    @Override
    public void ClearEventListOfBlockedNumbers() {
        int COUNT = preferences.getInt(NUM_BLOCK_LOG, 0);
        if(COUNT <= 0)return;
        for (int i = 0; i < COUNT; i++){
            editor.remove(BLOCKED_LOG+i);
        }
        editor.putInt(NUM_BLOCK_LOG, 0);
        editor.commit();
    }

    @Override
    public ArrayList<Pair<String, String>> GetBlockedIncomingCalls() {
        int COUNT = preferences.getInt(NUM_BLOCK_LOG, 0);
        if (COUNT <=0)return null;
        ArrayList<Pair<String, String>> data = new ArrayList<Pair<String, String>>();
        String[] Default = {"", ""};
        for (int i = (COUNT-1); i >= 0; i--){
            Set<String> set = preferences.getStringSet(BLOCKED_LOG+i, new HashSet<String>(Arrays.asList(Default)));
            String num = "";
            String date = "";
            int onlyTwo = 1;
            for(String s : set){
                if(onlyTwo == 1){
                    num = s;
                }else{
                    date = s;
                }
                onlyTwo++;
                if(onlyTwo > 2)break;
            }
            Pair<String, String> pair = new Pair<String, String>(num, date);
            data.add(pair);
        }
        return data;
    }

    @Override
    public void setServiceStatus(Boolean running) {
        editor.putBoolean(SERVICE_STATUS, running);
        editor.commit();
    }

    @Override
    public Boolean isServiceRunning() {
        return preferences.getBoolean(SERVICE_STATUS, false);
    }

    /**@return true if database was used before*/
    @Override
    public boolean CheckAvailability() {
        return !isEmpty;
    }
    /**@return HashSet of numbers to be blocked*/
    @Override
    public HashSet<String> GetNumbersFromDatabase() {
        if(isEmpty) return null;
        int COUNT = preferences.getInt(COUNT_OF_NUM, 0);
        if (COUNT <= 0) return null;
        HashSet<String> data = new HashSet<String>();
        for (int i = 0; i < COUNT; i++){
            data.add(preferences.getString(NUMBER+i, "error_number_not_in_preferences"));
            //TODO osetrovat error_number_not_in_preferences
        }
        return data;
    }
    /**Sets numbers from HashSet data to database
     * @return true if operation was successful*/
    @Override
    public boolean SetNumbersToDatabase(HashSet<String> data) {
        editor.putBoolean(AVAILABILITY, true);
        int counter = 0;
        for(String number : data){
            editor.putString(NUMBER+counter, number);
            counter++;
        }
        editor.putInt(COUNT_OF_NUM, counter);
        return editor.commit();
    }
    /**Adds number to database
     * @return true if operation was successful*/
    @Override
    public boolean AddNumberToDatabase(String number) {
        editor.putBoolean(AVAILABILITY, true);
        int COUNT = preferences.getInt(COUNT_OF_NUM, 0);
        editor.putString(NUMBER+COUNT, number);
        COUNT++;
        editor.putInt(COUNT_OF_NUM, COUNT);
        return editor.commit();
    }
    /**Removes number from database
     * @return true if operation was successful*/
    @Override
    public boolean RemoveNumberFromDatabase(String number) {
        int COUNT = preferences.getInt(COUNT_OF_NUM, 0);
        HashSet<String> newData = new HashSet<String>();
        boolean update = false;
        for (int i = 0; i < COUNT; i++){
            if((preferences.contains(NUMBER+i) && !(preferences.getString(NUMBER+i, ERROR)).equals(number))){
                newData.add(preferences.getString(NUMBER+i, ERROR));
            } else {
                update = true;
            }
        }
        if(update)return SetNumbersToDatabase(newData);
        return false;
    }
    /**Gets boolean with key "key"*/
    @Override
    public boolean GetBooleanValue(String key) {
        return preferences.getBoolean(key, false);
    }
    /**Gets string with key "key"*/
    @Override
    public String GetStringValue(String key) {
       return preferences.getString(key, null);
    }
    /**Sets boolean on key "key"
     * @return true if operation was successful*/
    @Override
    public boolean SetBooleanValue(String key, Boolean value) {
        if(key.contains(NUMBER) || key.equals(COUNT_OF_NUM))return false;
        editor.putBoolean(key, value);
        return editor.commit();
    }
    /**Sets string ok key "key"
     * @return true if operation was successful*/
    @Override
    public boolean SetStringValue(String key, String value) {
        if(key.contains(NUMBER) || key.equals(COUNT_OF_NUM))return false;
        editor.putString(key, value);
        return editor.commit();
    }
    /**@return  HashSet of couples String (key) and Object (Should be String, Int or Boolean) */
    @Override
    public HashMap<String, Object> LoadSettings() {
        //TODO
        return null;
    }
    /**Stores HashSet of couples String (key) and Object (Should be String, Int or Boolean) */
    @Override
    public void StoreSettings(HashMap<String, Object> settings) {
        //TODO
    }
    /**Increments number of calls which were blocked since CallBlockService was started for the first time*/
    @Override
    public int IncrementStats() {
        int num;
        if(preferences.contains(STATS)){
            num = preferences.getInt(STATS, 0) + 1;
        } else {
            num = 1;
        }
        editor.putInt(STATS, num);
        editor.commit();
        return num;
    }
    /**Gets number of calls which were blocked since CallBlockService was started for the first time*/
    @Override
    public int GetStats(){
        int num;
        if(preferences.contains(STATS)){
            num = preferences.getInt(STATS, 0);
        } else {
            num = 0;
            editor.putInt(STATS, num);
            editor.commit();
        }
        return num;
    }

}
