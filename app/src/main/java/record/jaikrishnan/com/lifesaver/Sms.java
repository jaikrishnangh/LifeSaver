package record.jaikrishnan.com.lifesaver;

import android.telephony.SmsManager;

/**
 * Created by JAI KRISHNAN on 07-03-2016.
 */
public class Sms {

    public void send() {

        String message = null;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+917418268186", null, message , null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
