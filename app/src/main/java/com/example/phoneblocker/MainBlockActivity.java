package com.example.phoneblocker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class MainBlockActivity extends Activity implements BindableWithCommunicator{





    final Context context = this;
    Communicator com = new Communicator(this);
    int PICK_CONTACT = 100;
    Activity activity = this;
    //  REMOVE GUI
    ArrayList<String> removeList=new ArrayList<String>();
    ArrayAdapter<String> removeAdapter;
    //
    //  NETWORKING
    HttpResponse httpResponse;
    ProgressDialog progressDialog;
    ProgressDialog downloadProgress;
    /*      NOVE        GLOBALNE        PREMENNE        */
    private boolean isBlocking = false;
    private DatabaseObject Database;
    private HashSet<String> NumbersDatabase;
    private int guiState;
    //

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        refreshSharedPref();
        NumbersDatabase = LoadDatabase();
        if(NumbersDatabase == null)NumbersDatabase = new HashSet<String>();
        isBlocking = Database.isServiceRunning();
        menuGui();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Waiting for server reply...");
        progressDialog.setCancelable(false);
        downloadProgress = new ProgressDialog(this);
        downloadProgress.setTitle("Loading...");
        downloadProgress.setMessage("Waiting for server reply...");
        downloadProgress.setCancelable(false);

	}
    private void menuGui(){
        guiState = 1;
        setContentView(R.layout.activity_main_block);
        setButtons();
        setTextView();
    }

    private  void removeGui(){
        guiState = 2;
        setContentView(R.layout.remove_layout);
        refreshSharedPref();
        NumbersDatabase = Database.GetNumbersFromDatabase();
        if (NumbersDatabase == null){
            NumbersDatabase = new HashSet<String>();
            Log.e("A", "SOM NULL");
            return;
        }
        removeList.clear();
        for(String number: NumbersDatabase){
            removeList.add(number);
        }
        removeAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, removeList);
        ListView list  = (ListView) findViewById(R.id.removeList);
        list.setAdapter(removeAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                removeNum(removeList.get(position));
                removeList.remove(position);
                removeAdapter.notifyDataSetChanged();
            }
        });
    }

    private void listGui(){
        guiState = 3;
        setContentView(R.layout.log_layout);
        refreshSharedPref();
        ArrayList<Pair<String, String>> logList = Database.GetBlockedIncomingCalls();
        if (logList == null){
            return;
        }
        ListView listView = (ListView)findViewById(R.id.logList);
        ArrayAdapter<Pair<String, String>> adapter = new ArrayAdapter<Pair<String, String>>(this, android.R.layout.simple_list_item_1, logList);
        listView.setAdapter(adapter);
    }

    private void reportGui(){
        guiState = 4;
        setContentView(R.layout.report_gui);
        LinearLayout checkButton = (LinearLayout) findViewById(R.id.checkL);
        LinearLayout reportButton = (LinearLayout) findViewById(R.id.reportL);
        LinearLayout downloadButton = (LinearLayout) findViewById(R.id.downloadL);
        checkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkNumber();
            }
        });
        reportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reportNumber();
            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadNumbers();
            }
        });
    }

    private void checkNumber(){
        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                String out = (String) msg.obj;
                Toast.makeText(getApplicationContext(), out, Toast.LENGTH_LONG).show();
                progressDialog.cancel();
            }
        };
        AlertDialog.Builder addDialog = new AlertDialog.Builder(MainBlockActivity.this);
        addDialog.setTitle("Check number");
        addDialog.setPositiveButton("Type number", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder DialogEdit = new AlertDialog.Builder(MainBlockActivity.this);
                final EditText input = new EditText(context);
                DialogEdit.setView(input);
                DialogEdit.setTitle("Type new number");
                DialogEdit.setCancelable(false);
                DialogEdit.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newNumber = (String) input.getText().toString();
                        if (!newNumber.equals("")) {
                            final HttpClient httpClient = new DefaultHttpClient();
                            Uri.Builder builder = new Uri.Builder();
                            builder.scheme("http")
                                    .authority("www.st.fmph.uniba.sk")
                                    .appendPath("~ivancik22")
                                    .appendPath("API_blocker.php")
                                    .appendQueryParameter("command", "check")
                                    .appendQueryParameter("user", "4746")
                                    .appendQueryParameter("number", newNumber);
                            String myUrl = builder.build().toString();
                            final HttpGet httpGet = new HttpGet(myUrl);

                            final Thread thread = new Thread(){
                              @Override
                                public void run(){
                                        Message message = new Message();
                                  try {
                                      httpResponse = httpClient.execute(httpGet);
                                      String content = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                                      message.obj = "Server: "+content;
                                      if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 1)");
                                  } catch (ClientProtocolException e) {
                                      message.obj = "Server: "+e.getMessage();
                                      if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 2)");
                                      Log.e("CallBlocker", "error: " + e.getMessage());
                                  } catch (IOException e) {
                                      message.obj = "Server: "+e.getMessage();
                                      if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 3)");
                                      e.printStackTrace();
                                      Log.e("CallBlocker", "error: "+e.getMessage());
                                  }
                              }
                            };
                            thread.start();
                            progressDialog.setButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    thread.interrupt();
                                    progressDialog.cancel();
                                }
                            });
                            progressDialog.show();
                        }
                    }
                });

                DialogEdit.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                DialogEdit.show();
            }
        });
        addDialog.show();
    }

    private void reportNumber(){
        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                String out = (String) msg.obj;
                Toast.makeText(getApplicationContext(), out, Toast.LENGTH_LONG).show();
                progressDialog.cancel();
            }
        };
        AlertDialog.Builder addDialog = new AlertDialog.Builder(MainBlockActivity.this);
        addDialog.setTitle("Report number");
        addDialog.setPositiveButton("Type number", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder DialogEdit = new AlertDialog.Builder(MainBlockActivity.this);
                final EditText input = new EditText(context);
                DialogEdit.setView(input);
                DialogEdit.setTitle("Type new number");
                DialogEdit.setCancelable(false);
                DialogEdit.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newNumber = (String) input.getText().toString();
                        if (!newNumber.equals("")) {
                            //http://www.st.fmph.uniba.sk/~ivancik22/API_blocker.php?command=report&&user=4746&&number=2560
                            final HttpClient httpClient = new DefaultHttpClient();
                            Uri.Builder builder = new Uri.Builder();
                            builder.scheme("http")
                                    .authority("www.st.fmph.uniba.sk")
                                    .appendPath("~ivancik22")
                                    .appendPath("API_blocker.php")
                                    .appendQueryParameter("command", "report")
                                    .appendQueryParameter("user", "4746")
                                    .appendQueryParameter("number", newNumber);
                            String myUrl = builder.build().toString();
                            final HttpGet httpGet = new HttpGet(myUrl);
                            final Thread thread = new Thread(){
                                @Override
                                public void run(){
                                    Message message = new Message();
                                    try {
                                        httpResponse = httpClient.execute(httpGet);
                                        String content = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                                        message.obj = "Server: "+content;
                                        if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 1)");
                                    } catch (ClientProtocolException e) {
                                        message.obj = "Error (check internet connection)x1";
                                        if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 2)");
                                        Log.e("CallBlocker", "error: " + e.getMessage());
                                    } catch (IOException e) {
                                        message.obj = "Error (check internet connection)x2";
                                        if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 3)");
                                        e.printStackTrace();
                                        Log.e("CallBlocker", "error: "+e.getMessage());
                                    }
                                }
                            };
                            thread.start();
                        }
                    }
                });
                DialogEdit.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                DialogEdit.show();
            }
        });
        addDialog.show();
    }

    private void downloadNumbers(){
        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                Log.d("CallBlocker", "download handler, I have message!");
                String out = (String) msg.obj;
                if(msg.arg1 == 0)Toast.makeText(getApplicationContext(), out, Toast.LENGTH_LONG).show();
                if(msg.arg1 == 1){
                    Toast.makeText(getApplicationContext(), "Processing...", Toast.LENGTH_LONG).show();
                    String[] numbers = out.split("&");
                    if(numbers.length > 2){
                        for(int i = 0; i < (numbers.length-1); i++){
                            String number = "+421"+numbers[i].substring(1);
                            NumbersDatabase.add(number);
                            if(isBlocking){
                                com.AddNumberToBlockList(number);
                            }else{
                                Database.AddNumberToDatabase(number);
                            }
                        }
                    }
                }
                progressDialog.cancel();
            }
        };
       final HttpClient httpClient = new DefaultHttpClient();
       Uri.Builder builder = new Uri.Builder();
       builder.scheme("http")
             .authority("www.st.fmph.uniba.sk")
             .appendPath("~ivancik22")
             .appendPath("API_blocker.php")
             .appendQueryParameter("command", "download")
             .appendQueryParameter("user", "4746");
        String myUrl = builder.build().toString();
        final HttpGet httpGet = new HttpGet(myUrl);
        final Thread thread = new Thread(){
                                @Override
                                public void run(){
                                    Message message = new Message();
                                    try {
                                        httpResponse = httpClient.execute(httpGet);
                                        message.arg1 = 1;
                                        message.obj = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                                        if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 1)");
                                    } catch (ClientProtocolException e) {
                                        message.arg1 = 0;
                                        message.obj = "Server: "+e.getMessage();
                                        if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 2)");
                                        Log.e("CallBlocker", "error: " + e.getMessage());
                                    } catch (IOException e) {
                                        message.arg1 = 0;
                                        message.obj = "Server: "+e.getMessage();
                                        if(!handler.sendMessage(message))Log.e("CallBlocker", "error while sending message (handler 3)");
                                        e.printStackTrace();
                                        Log.e("CallBlocker", "error: "+e.getMessage());
                                    }
                                }
                            };
                            thread.start();
                            progressDialog.setButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    thread.interrupt();
                                    progressDialog.cancel();
                                }
                            });
                            progressDialog.show();
    }

    private void refreshSharedPref(){
        Database = new SharedPreferencesDatabase(this);
    }

    private void removeNum(String number){
        if(isBlocking){
            com.RemoveNumberFromBlockList(number);
            NumbersDatabase.remove(number);
            refreshSharedPref();
        }else{
            NumbersDatabase.remove(number);
            Database.RemoveNumberFromDatabase(number);
        }
    }

    private void addNumber(){
        AlertDialog.Builder addDialog = new AlertDialog.Builder(MainBlockActivity.this);

        addDialog.setTitle("Add new number");
        addDialog.setPositiveButton("Type number", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder DialogEdit = new AlertDialog.Builder(MainBlockActivity.this);
                        final EditText input = new EditText(context);
                        DialogEdit.setView(input);
                        DialogEdit.setTitle("Type new number");
                        DialogEdit.setCancelable(false);
                        DialogEdit.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String newNumber = (String) input.getText().toString();
                                        if (!newNumber.equals("")) {
                                                //TODO number validity test
                                            NumbersDatabase.add(newNumber);
                                            if(isBlocking){
                                                com.AddNumberToBlockList(newNumber);
                                            }else{
                                                Database.AddNumberToDatabase(newNumber);
                                            }
                                            Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                        DialogEdit.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        DialogEdit.show();

                    }
                });

      /*  addDialog.setNegativeButton("Open Contacts", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                        startActivityForResult(intent, PICK_CONTACT);
                    }
                });*/

        addDialog.show();
    }
    private void setButtons(){
        LinearLayout addNum = (LinearLayout) findViewById(R.id.add_num);
        LinearLayout remNum = (LinearLayout) findViewById(R.id.rem_num);
        LinearLayout blockList = (LinearLayout) findViewById(R.id.block_list);
        LinearLayout report = (LinearLayout) findViewById(R.id.report);
        LinearLayout serviceControl = (LinearLayout) findViewById(R.id.service);
        addNum.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addNumber();
            }
        });
        remNum.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               removeGui();
            }
        });
        blockList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                listGui();
            }
        });
        report.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               reportGui();
            }
        });
        serviceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBlocking){
                    stopService();
                    if (com.isBounded())com.Disconnect();
                    isBlocking = false;
                    setServiceButtonText("Start blocking.");
                }else{
                    startService();
                    isBlocking = true;
                    setServiceButtonText("Stop blocking.");
                }
            }
        });
    }
    public void startService(){
        startService(new Intent(this, CallBlockService.class));
    }
    public void stopService(){
        stopService(new Intent(this, CallBlockService.class));
    }
    private void setTextView(){
        if(isBlocking){
            setServiceButtonText("Stop blocking.");
        }else{
            setServiceButtonText("Start blocking.");
        }
    }
    private void setServiceButtonText(String s){
        TextView text = (TextView) findViewById(R.id.textView5);
        text.setText(s);
        if(isBlocking){
            layoutBackground(((RelativeLayout)findViewById(R.id.AcMainL)), "#AFE3BF");
        }else{
            layoutBackground(((RelativeLayout)findViewById(R.id.AcMainL)), "#DE4B5F");
        }
    }
    private void layoutBackground(RelativeLayout layout, String color){
        layout.setBackgroundColor(Color.parseColor(color));
    }
    private HashSet<String> LoadDatabase(){
        HashSet<String> data = null;
        if(Database.CheckAvailability())data = Database.GetNumbersFromDatabase();
        if(data == null) data = new HashSet<String>();
        return data;
    }

    //TODO IMPORTANT v main activity MUSIA byt tieto dve metody. Pouziva ich communicator.
    public boolean doBind(ServiceConnection con){
        Intent intent = new Intent(this, CallBlockService.class);
        Boolean tmp = bindService(intent, con, Context.BIND_AUTO_CREATE);
        if (!tmp)Log.e("MainActivity ERROR", "bind unsuccessful.");
        return tmp;
    }
    public void doUnbind(ServiceConnection con){
        unbindService(con);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_block, menu);
		return true;
	}
    @Override
    public void onBackPressed(){
        if(guiState == 1){
            finish();
        }else{
            menuGui();
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    @Override
    public void onDestroy(){
        if (com.isBounded())com.Disconnect();
        super.onDestroy();
    }

}
