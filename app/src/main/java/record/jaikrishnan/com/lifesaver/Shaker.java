package record.jaikrishnan.com.lifesaver;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Shaker extends AppCompatActivity implements SensorListener {

    MediaPlayer mediaPlayer;
    boolean finish = true;
    String message;
    GPSTracker gpsTracker;
    Geocoder geocoder;
    List<Address> addresses;
    static final int check = 1111;
    static String[] numbers;
    // For shake motion detection.
    private SensorManager sensorMgr;
    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 700;
    private ImageView imageView;
    private AlertDialog alertDialog;
    private FloatingActionButton fab;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         // other initializations
        // start motion detection
        setContentView(R.layout.main_activity);
        mediaPlayer = MediaPlayer.create(this,R.raw.siren);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Shaker.this, Account.class));
            }
        });
        Account a = new Account();
        DB entry = new DB(Shaker.this);
        entry.open();
        String res = entry.getData();
         numbers = res.split(" ");
        entry.close();
//         numbers = a.values();
        imageView = (ImageView) findViewById(R.id.rec);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(Shaker.this,"Please connect With internet",Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak");
                startActivityForResult(i, check);
            }
        });
        gpsTracker = new GPSTracker(Shaker.this);
        //help = (Button) findViewById(R.id.helpme);
        geocoder = new Geocoder(this, Locale.getDefault());

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        boolean accelSupported = sensorMgr.registerListener(this,
                SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);

        if (!accelSupported) {
            // on accelerometer on this device
            sensorMgr.unregisterListener(this,
                    SensorManager.SENSOR_ACCELEROMETER);
        }
    }

    protected void onPause() {
        if (sensorMgr != null) {
            sensorMgr.unregisterListener(this,
                    SensorManager.SENSOR_ACCELEROMETER);
            sensorMgr = null;
        }
        super.onPause();
    }

    public void onAccuracyChanged(int arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x = values[SensorManager.DATA_X];
                y = values[SensorManager.DATA_Y];
                z = values[SensorManager.DATA_Z];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)
                        / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    if (finish) {
                        finish = false;
                    final AlertDialog alertDialog = new AlertDialog.Builder(
                            Shaker.this).create();

                    // Setting Dialog Title
                    alertDialog.setTitle("Life Saver Alert");

                    // Setting Dialog Message
                    alertDialog.setMessage("Your Location Will be sent within 5 secs\n Don't need to share click No");

                    // Setting Icon to Dialog
                    alertDialog.setIcon(R.drawable.error);

                    // Setting OK Button
                    alertDialog.setButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to execute after dialog closed
                            finish = true;
                            Toast.makeText(Shaker.this, "Alert Not Sent", Toast.LENGTH_SHORT).show();
                            return;
                            //Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!finish) {
                            alertDialog.cancel();
                                mediaPlayer.start();
                            Toast.makeText(Shaker.this, "Alert Sent", Toast.LENGTH_SHORT).show();
                            gpsTracker = new GPSTracker(Shaker.this);
                            if (gpsTracker.canGetLocation()) {
                                double latitude = gpsTracker.getLatitude();
                                double longitude = gpsTracker.getLongitude();
                                try {
                                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                String state = addresses.get(0).getAdminArea();
                                String country = addresses.get(0).getCountryName();
                                String postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName();
                                Toast.makeText(Shaker.this, "Address " + address + "\nCity " + city +
                                        "\nState " + state + "\nCountry " + country + "\nPostal Code " + postalCode + "\nKnownName " + knownName, Toast.LENGTH_SHORT).show();

                                message = "Address " + address + "\nCity " + city +
                                        "\nState " + state + "\nCountry " + country + "\nPostal Code " + postalCode + "\nKnownName " + knownName;


                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage("+91"+numbers[0], null, "Hi I need Help I m living in "+message , null, null);
                                    smsManager.sendTextMessage("+91"+numbers[1], null, "Hi I need Help I m living in "+message , null, null);
                                     Toast.makeText(getApplicationContext(), "SMS sent."+numbers[0]+" "+numbers[1], Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            } else {
                                //Toast.makeText(getApplicationContext(),"Turn on your GPS",Toast.LENGTH_SHORT).show();
                                // gpsTracker.showSettingsAlert();
                            }
                        }
                        }
                    }, 5000);
                }
            }
            }
        }
                last_x = x;
                last_y = y;
                last_z = z;
            }

    public void dialogTimer(String message){
        final AlertDialog alertDialog = new AlertDialog.Builder(
                Shaker.this).create();

        // Setting Dialog Title
        alertDialog.setTitle("Alert Dialog");

        // Setting Dialog Message
        alertDialog.setMessage("Welcome to AndroidHive.info");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.mipmap.ic_launcher);

        // Setting OK Button
        alertDialog.setButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed

                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alertDialog.cancel();

                Toast.makeText(Shaker.this,"Handler",Toast.LENGTH_SHORT).show();
            }
        }, 5000);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == check && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+91"+numbers[0], null, results.get(0), null, null);
            smsManager.sendTextMessage("+91"+numbers[1], null, results.get(0), null, null);
            // smsManager.sendTextMessage("+919042864423", null, results.get(0) , null, null);
            Toast.makeText(getApplicationContext(), "Recorded voice SMS sent", Toast.LENGTH_LONG).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}





