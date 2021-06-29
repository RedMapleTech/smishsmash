package tech.redmaple.smishsmash.analysis;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import tech.redmaple.smishsmash.sms.Inbox;
import tech.redmaple.smishsmash.sms.SMS;
import timber.log.Timber;

public class Messages {

    /**
     * Get all the SMS messages as AnalysedMessage objects
     * @param context of the app
     */
    public static AnalysedMessage[] getMessages(Context context) {
        // get all messages
        List<SMS> msgs = Inbox.getAllSMSFromContentResolver(context);

        int totalSMS = msgs.size();
        Timber.d("getMessages got %d messages from inbox", totalSMS);
        AnalysedMessage[] analysedMessages = new AnalysedMessage[totalSMS];

        Timber.d("analyse: Running...");

        // setup date format
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        // use msgData
        for (int i = 0; i < totalSMS; i++) {
            SMS msg = msgs.get(i);
            Timber.d("Retreiving message %s/%d, received from %s at %s UTC", msg.getId(), totalSMS, msg.getAddress(), dateFormat.format(Long.parseLong(msg.getTime())));

            AnalysedMessage analysis = new AnalysedMessage();
            analysis.setMessage(msg);

            // store it for return
            analysedMessages[i] = analysis;
        }

        Timber.d("analyse: Analysis of all messages complete.");

        return analysedMessages;
    }
}
