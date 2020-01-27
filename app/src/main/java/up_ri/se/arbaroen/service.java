package up_ri.se.arbaroen;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class service extends Service
{
    private static final String TAG = "ButtonService";




    public final String ACTION_USB_PERMISSION = "up_ri.se.arbaroen.USB_PERMISSION";

    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    private AudioManager myAudioManager;



    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");

                if(data.equals("HOME")) {
                    onClickHome();
                }

                else if(data.equals("MUTE")) {
                    onClickMute();
                }
                else if (data != null && data instanceof String) volume(data);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);

                            Toast.makeText(context, "Buttons control started",Toast.LENGTH_LONG).show();
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {


                onStartCommand(intent,0,500);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onStop();
            }
        }
    };



    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "My Service created", Toast.LENGTH_LONG).show();
        Log.d("TAG", "Buttons Service created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAG", "Service started.");
        Toast.makeText(this, "Buttons Service started", Toast.LENGTH_LONG).show();

        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if(this.manageUSBPermissions()) {


            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(broadcastReceiver, filter);
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo("up_ri.se.arbaroen", 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


            IBinder b = ServiceManager.getService(USB_SERVICE);
            IUsbManager serviceUsb = IUsbManager.Stub.asInterface(b);

            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

            if (!usbDevices.isEmpty()) {
                boolean keep = true;
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    device = entry.getValue();
                    int deviceVID = device.getVendorId();
                    if (deviceVID == 0x1a86)//Arduino Vendor ID
                    {
                        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(device, pi);
                        try {
                            serviceUsb.grantDevicePermission(device, ai.uid);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }

                    if (!keep)
                        break;
                }

            }
        }
        else {

        }




        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
    public void onDestroy() {
        Toast.makeText(this, "Buttons Stopped", Toast.LENGTH_LONG).show();
    }


    public void onStop() {

        serialPort.close();

    }


    public void onClickMute() {
        myAudioManager.adjustVolume(AudioManager.ADJUST_TOGGLE_MUTE,1);
    }

    public void onClickHome() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }



    private void volume (String rotaryDataString) {

        if(rotaryDataString.equals("PLUS")) {
            myAudioManager.adjustVolume(AudioManager.ADJUST_RAISE,1);
        }
        else if(rotaryDataString.equals("MOINS")) {
            myAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, 1);
        }
    }

    /*This method change song in spotify with 1 rotary encoder, maybe change radio too in future version*/


    private void seek (String rotaryDataString) {
        if(myAudioManager.isMusicActive()){

            if(rotaryDataString.equals("SEEK-")) {
                myAudioManager.adjustVolume(AudioManager.ADJUST_RAISE,1);

            }
            else if(rotaryDataString.equals("SEEK+")) {
                myAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, 1);
            }
        }


    }

    /**
     * Verify if the application is a system app and has MANAGE_USB permission
     * before granting the USB permission for you specific USB devices
     */
    private boolean manageUSBPermissions() {
        if ((this.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            Log.i(TAG,"This is a system application");
            if (getApplicationContext().checkCallingOrSelfPermission("android.permission.MANAGE_USB") == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG,"I have android.permission.MANAGE_USB");
                Toast.makeText(this, "Authorised", Toast.LENGTH_LONG).show();
                grantUsbPermissions();
                return true;
            } else {
                Log.i(TAG,"I do not have android.permission.MANAGE_USB");
                Toast.makeText(this, "!!!buttons Not authorised !!!", Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            Log.i(TAG,"This is not a system application");
            Toast.makeText(this, "No system application", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * This is to avoid the android usb host permission confirmation dialog
     * The application need to be a system app and have MANAGE_USB permission for it to work
     */
    public void grantUsbPermissions() {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo( "up_ri.se.arbaroen", 0 );
            if( ai != null ) {
                UsbManager manager = (UsbManager) getSystemService( Context.USB_SERVICE );
                IBinder b = ServiceManager.getService( Context.USB_SERVICE );
                IUsbManager service = IUsbManager.Stub.asInterface( b );

                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while( deviceIterator.hasNext() ) {
                    UsbDevice device = deviceIterator.next();
                    if ( device.getVendorId() == 0x1a86 ) {
                        Toast.makeText(this, "Found Arduino device", Toast.LENGTH_LONG).show();
                        service.grantDevicePermission( device, ai.uid );
                        service.setDevicePackage( device, "up_ri.se.arbaroen", ai.uid );
                    }
                }
            }
        }
        catch ( Exception e ) {
            Log.e(TAG, "Error granting USB permissions: " + e);
        }
    }
}


