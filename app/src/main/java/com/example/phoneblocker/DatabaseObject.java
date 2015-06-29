package com.example.phoneblocker;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Marek on 12/19/2014.
 * Interface for class which wants to provide saving numbers to database (sharedPreferences, File, SQL)
 */
public interface DatabaseObject {
    /**@return true if database was used before*/
    public boolean CheckAvailability();
    /**@return HashSet of numbers to be blocked*/
    public HashSet<String> GetNumbersFromDatabase();
    /**Sets numbers from HashSet data to database
     * @return true if operation was successful*/
    public boolean SetNumbersToDatabase(HashSet<String> data);
    /**Adds number to database
     * @return true if operation was successful*/
    public boolean AddNumberToDatabase(String number);
    /**Removes number from database
     * @return true if operation was successful*/
    public boolean RemoveNumberFromDatabase(String number);
    /**Gets boolean with key "key"*/
    public boolean GetBooleanValue(String key);
    /**Gets string with key "key"*/
    public String GetStringValue(String key);
    /**Sets boolean on key "key"
     * @return true if operation was successful*/
    public boolean SetBooleanValue(String key, Boolean value);
    /**Sets string ok key "key"
     * @return true if operation was successful*/
    public boolean SetStringValue(String key, String value);
    /**@return  HashSet of couples String (key) and Object (Should be String, Int or Boolean) */
    //TODO change system of saving Settings. <String, Object> is not lucky solution.
    public HashMap<String, Object> LoadSettings();
    /**Stores HashSet of couples String (key) and Object (Should be String, Int or Boolean) */
    //TODO change system of saving Settings. <String, Object> is not lucky solution.
    public void StoreSettings(HashMap<String, Object> settings);
    /**Increments number of calls which were blocked since CallBlockService was started for the first time*/
    public int IncrementStats();
    /**Gets number of calls which were blocked since CallBlockService was started for the first time*/
    public int GetStats();

    public void AddLastBlockedNumber(Set<String> set);

    public void ClearEventListOfBlockedNumbers();

    public ArrayList<Pair<String, String>> GetBlockedIncomingCalls();

    public void setServiceStatus(Boolean running);

    public Boolean isServiceRunning();
}

