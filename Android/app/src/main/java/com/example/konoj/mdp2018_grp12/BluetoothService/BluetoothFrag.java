package com.example.konoj.mdp2018_grp12.BluetoothService;

/**
 * Created by konoj on 26/1/2018.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.konoj.mdp2018_grp12.Map.MyView;
import com.example.konoj.mdp2018_grp12.Map.PixelGridView;
import com.example.konoj.mdp2018_grp12.R;
import com.example.konoj.mdp2018_grp12.RepeatListener;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothFrag extends Fragment implements MyView.OnToggledListener,SensorEventListener {

    public BluetoothFrag(){

    }

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    public static final String MY_PREFERENCE = "MyPref";
    public static final String FUNCTION_1 = "function1String";
    public static final String FUNCTION_2 = "function2String";
    View view;


    SharedPreferences sharedPreferences;

    // Layout Views
    private TextView statusText;
    private Button fwdButton;
    private Button backButton;
    private Button leftButton;
    private Button rightButton;
    private Button updateButton;
    private Button fastestButton;
    private Button exploreButton;
    private TextView timer;
    private TextView fastTimer;
    private TextView mdfText;
    private TextView mdf2Text;
    private Button setButton;
    private static TextView xPos;
    private static TextView yPos;
    PixelGridView pixelGrid;
    private boolean isAutoUpdate = true;
    private boolean listenForUpdate = false;
    private String tempMsg;
    private SensorManager SM;
    long startExploreTime1 = 0;
    long startFastTime2 =0;
    private boolean stillExplore = true;
    private boolean stillFast = true;

    private String globalMdf1;
    private String globalMdf2;




    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        sharedPreferences = getActivity().getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

       /* SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);*/



    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView       = inflater.inflate(R.layout.bluetooth_fragment, container, false);

        xPos=(TextView) rootView.findViewById(R.id.x_coor);
        yPos=(TextView)rootView.findViewById(R.id.y_coor);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Initial setup for map UI
        statusText = (TextView) view.findViewById(R.id.StatusText);
        fwdButton = (Button) view.findViewById(R.id.fwd);
        backButton = (Button) view.findViewById(R.id.back);
        leftButton = (Button) view.findViewById(R.id.left);
        rightButton = (Button) view.findViewById(R.id.right);
        updateButton=(Button)view.findViewById(R.id.receiveGRID);
        fastestButton=(Button)view.findViewById(R.id.fast);
        exploreButton=(Button)view.findViewById(R.id.explore);
        setButton=(Button)view.findViewById(R.id.set_button);
        mdfText=(TextView)view.findViewById(R.id.mdfText);
        mdf2Text=(TextView)view.findViewById(R.id.mdf2);
        timer=(TextView)view.findViewById(R.id.exploreTimer);
        fastTimer=(TextView)view.findViewById(R.id.fastestTimer);
        pixelGrid=(PixelGridView)view.findViewById(R.id.pixelGridView);
        pixelGrid.setNumColumns(15);
        pixelGrid.setNumRows(20);
        pixelGrid.invalidate();
        final ToggleButton toggle = (ToggleButton) view.findViewById(R.id.mode_toggle);
        toggle.setTextOff("MANUAL");
        toggle.setTextOn("AUTO");
        toggle.setChecked(true);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggle.setText("AUTO");
                    //sendMessage("sendArena");
                    isAutoUpdate = true;
                    updateButton.setEnabled(false);

                } else {
                    toggle.setText("MANUAL");
                    isAutoUpdate = false;
                    updateButton.setEnabled(true);
                }
            }
        });

        //Disable unnecessary buttons
        setButton.setEnabled(false);
        fastestButton.setEnabled(false);
        updateButton.setEnabled(false);

        //Set global view
        this.view=view;

    }

    // Function to set waypoint coordinate
    public static void setCoor(int x,int y){
        xPos.setText(String.valueOf(x));
        yPos.setText(String.valueOf(y));
    }

    // Handler for exploration timer function
    Handler timerHandler1 = new Handler();
    Runnable timerRunnable1 = new Runnable() {
        @Override
        public void run() {
            if(!stillExplore){
                return;
            }
                long millis = System.currentTimeMillis() - startExploreTime1;
                int seconds = (int) (millis / 1000.0);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timer.setText("Exploration Time : " + String.format("%d:%d", ((int) minutes), ((int) seconds)));

                timerHandler1.postDelayed(this, 0);
            }

    };

    // Handler for fastest path timer function
    Handler timerHandler2 = new Handler();
    Runnable timerRunnable2 = new Runnable() {
        @Override
        public void run() {
            if(!stillFast){
                return;
            }
            long millis = System.currentTimeMillis() - startFastTime2;
            int seconds = (int) (millis / 1000.0);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            fastTimer.setText("Fastest Path Time : "+String.format("%d:%d", ((int)minutes), ((int)seconds)));
            timerHandler2.postDelayed(this, 0);
        }
    };


    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");



        fastestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("beginFastest");


            }
        });

        exploreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("beginExplore");
            }
        });

       updateButton.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View view) {
               sendMessage("sendArena");
               if(tempMsg!=null) {
                   pixelGrid.updateDemoRobotPos(tempMsg);
               }
               listenForUpdate=true;

           }
       });

       fwdButton.setOnTouchListener(new RepeatListener(400,100, new OnClickListener(){
           @Override
           public void onClick(View view) {

                   sendMessage("f");

           }
       }));

        backButton.setOnTouchListener(new RepeatListener(400,100, new OnClickListener(){
            public void onClick(View v){

                 {
                    sendMessage("r");
                }
            }
        }));

        leftButton.setOnTouchListener(new RepeatListener(400,100, new OnClickListener(){
            public void onClick(View v){

                    sendMessage("tl");

            }
        }));

        rightButton.setOnTouchListener(new RepeatListener(400,100, new OnClickListener(){
            public void onClick(View v){

                    sendMessage("tr");

            }
        }));

        setButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    Toast.makeText(getActivity(), "Waypoint x:"+xPos.getText().toString()+", y:"+yPos.getText().toString()+" set", Toast.LENGTH_SHORT).show();

                    pixelGrid.wpShow();
                    pixelGrid.setWaypoint(Integer.parseInt(xPos.getText().toString()),Integer.parseInt(yPos.getText().toString()));
                    pixelGrid.invalidate();
                    sendMessage("WAYPOINT "+yPos.getText().toString()+" "+xPos.getText().toString());
            }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything


        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if(message.equals("beginFastest")){
            stillFast=true;
            startFastTime2 = System.currentTimeMillis();
            timerHandler2.postDelayed(timerRunnable2,0);

           disableDirection();
        }

        if(message.equals("beginExplore")){
            stillExplore=true;
            exploreButton.setEnabled(false);
            startExploreTime1 = System.currentTimeMillis();
            timerHandler1.postDelayed(timerRunnable1, 0);

           disableDirection();
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };



    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    switch(writeMessage) {
                        case "f":
                            writeMessage = "Robot Moving NORTH";
                            break;
                        case "r":
                            writeMessage = "Robot Moving SOUTH";
                            break;
                        case "tl":
                            writeMessage = "Robot Turning WEST";
                            break;
                        case "tr":
                            writeMessage = "Robot Turning EAST";
                            break;
                        case "beginExplore":
                            writeMessage = "Roboto Starting Exploration";
                            break;
                        case "beginFastest":
                            writeMessage = "Roboto Starting Fastest Path";
                            break;
                    }
                    statusText.setText(writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] read = (byte[]) msg.obj;
                    String readMsg = new String(read);  // byte[]; offset; byteCount
                    Log.e("DebugGrid", "readMsg: " + readMsg);
                    //statusText.setText(readMsg);

                    if(readMsg.contains("MDF")){
                        //Fix to prevent old MDF from repeating
                        String tempString[]=readMsg.split("\n");
                        String grid[]=tempString[0].split("\\|");

                        //Get robot position and direction
                        String P2=grid[3]+"|"+grid[4]+"|"+grid[5];

                        //Map update code
                        if(isAutoUpdate==true||listenForUpdate==true) {
                            //Show robot status
                            statusText.setText("Robot Moving " + grid[3]+" to ["+grid[4]+", "+grid[5]+"]");
                            mdfText.setText(grid[1]);
                            mdf2Text.setText(grid[2]);
                            globalMdf1=grid[1];
                            globalMdf2=grid[2];
                            pixelGrid.updateRobotPos(P2);
                            pixelGrid.invalidate();

                            String mapObject = grid[1];
                            String obstacleMap = grid[2];
                            pixelGrid.updateArena(obstacleMap, mapObject);

                            listenForUpdate=false;
                        }

                    }
                    if(readMsg.contains("grid")) {
                        try {
                            Log.e("MDF","MDF: "+readMsg);
                            JSONObject obj = new JSONObject(readMsg);
                            String P2 = obj.getString("grid");

                            if(isAutoUpdate==true||listenForUpdate==true) {
                                //Update obstacle map
                                pixelGrid.updateDemoArenaMap(P2);
                                pixelGrid.invalidate();
                                listenForUpdate=false;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                    else if(readMsg.contains("robotPosition")){
                        if(isAutoUpdate==true) {
                            //update robot position
                            pixelGrid.updateDemoRobotPos(readMsg);
                            pixelGrid.invalidate();
                        }else{
                            tempMsg=readMsg;
                        }

                    }
                        else if (readMsg.contains("ENDEXPLORE")){
                        //Stop timer and enable waypoint button when exploration ends
                        stillExplore=false;
                        exploreButton.setEnabled(true);
                        fastestButton.setEnabled(true);
                        setButton.setEnabled(true);
                       enableDirection();
                        }
                        else if (readMsg.contains("ENDFASTEST")){
                        //Stop timer when fastest path end
                        stillFast=false;
                        pixelGrid.invalidate();
                       enableDirection();
                    }
                    //Read message for Fastest Path (Only robot position)
                        else if (readMsg.contains("FP")){
                        String tempString[]=readMsg.split("\n");
                        String grid[]=tempString[0].split("\\|");
                        Log.e("Grid Info",grid[1]);
                        String P2=grid[1]+"|"+grid[2]+"|"+grid[3];

                        statusText.setText("Robot Moving "+grid[1]+" to ["+grid[2]+", "+grid[3]+"]");
                        //update robot position
                        pixelGrid.updateRobotPos(P2);
                        pixelGrid.invalidate();
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    //Enable directional buttons
    private void enableDirection(){
        fwdButton.setEnabled(true);
        leftButton.setEnabled(true);
        rightButton.setEnabled(true);
        backButton.setEnabled(true);
    }
    //Disable directional buttons
    private void disableDirection(){
        fwdButton.setEnabled(false);
        leftButton.setEnabled(false);
        rightButton.setEnabled(false);
        backButton.setEnabled(false);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;

            case R.id.calib:
                sendMessage("CALIB");
                return true;

            case R.id.kill:
                sendMessage("KILL");
                return true;

            case R.id.settings:
                Intent settingsIntent = new Intent(getActivity(), ActivitySettings.class);
                startActivity(settingsIntent);
                return true;

            case R.id.btnF1:
                String getFunction1String = sharedPreferences.getString(FUNCTION_1,"NULL");
                sendMessage(getFunction1String);
                return true;

            case R.id.btnF2:
                String getFunction2String = sharedPreferences.getString(FUNCTION_2,"NULL");
                sendMessage(getFunction2String);
                return true;

            case R.id.showMDF:

                if(!stillExplore){
                    Intent i =new Intent(getActivity(),MDFActivity.class);
                    i.putExtra("mdf1",globalMdf1);
                    i.putExtra("mdf2",globalMdf2);
                    startActivity(i);
                }

                return true;

            case R.id.clearMap:
                clearMap();
                return true;

            case R.id.devMode:

                exploreButton.setEnabled(true);
                fastestButton.setEnabled(true);
                setButton.setEnabled(true);
                enableDirection();
                Toast.makeText(getActivity(),"All functions enabled",Toast.LENGTH_SHORT).show();
                return true;

            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }


    //Method to clear map
    private void clearMap(){
        pixelGrid.clearMap();
        pixelGrid.invalidate();
        pixelGrid.wpHide();
        Toast.makeText(getActivity(),"Map Cleared",Toast.LENGTH_SHORT).show();
    }



    @Override
    public void OnToggled(MyView v, boolean touchOn) {
        //get the id string

        String idString = v.getIdX() + ":" + v.getIdY();

        Toast.makeText(getActivity(),idString,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
