package tech.redmaple.smishsmash.sms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Inbox {

    private static final String SMS = "content://sms";

    //https://stackoverflow.com/questions/36001339/find-and-interate-all-sms-mms-messages-in-android
    private static final String SMS_MMS_URI = "content://mms-sms/complete-conversations";
    private static final String SMS_INBOX_URI = "content://sms/inbox";

    /**
     * Uses the content resolver to get all the SMS messages on the device
     *
     * @param context for the App
     * @return array of SMS objects representing the content of the messages
     */
    @Deprecated // in favour of the API19 stuff below - e.g. don't have to parse URIs
    public static List<SMS> getAllSMSFromContentResolverLegacy(Context context) {
        List<SMS> allSMS = new ArrayList<>();

        String[] proj = {"*"};

        //String[] projection = {Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN};
        Cursor cursor = context.getContentResolver().query(Uri.parse(SMS), proj, null, null, null);
        int totalSMS = cursor.getCount();

        Timber.d("Content resolver found %d SMS", totalSMS);

        if (cursor.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                SMS objSms = new SMS();

                // https://stackoverflow.com/questions/1976252/how-to-use-sms-content-provider-where-are-the-docs
                objSms.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                objSms.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                objSms.setMsg(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                objSms.setTime(cursor.getString(cursor.getColumnIndexOrThrow("date")));

                /*if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }*/

                allSMS.add(objSms);
                cursor.moveToNext();
            }
        }

        cursor.close();

        //cursor = context.getContentResolver().query(Uri.parse("content://mms"), null, null, null, null);
        //int totalMMS = cursor.getCount();
        //Timber.d("Content resolver found %d MMS", totalMMS);

        return allSMS;
    }

    /**
     * Uses the content resolver to get all the SMS messages on the device
     * @param context
     * @return
     */
    public static List<SMS> getAllSMSFromContentResolver(Context context) {

        Timber.d("Default SMS app: %s", Telephony.Sms.getDefaultSmsPackage(context));

        List<SMS> allSMS = new ArrayList<>();

        // https://stackoverflow.com/questions/19856338/using-new-telephony-content-provider-to-read-sms

        ContentResolver cr = context.getContentResolver();
        String[] fields = new String[] { Telephony.Sms.Inbox._ID, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.DATE_SENT};

        // Uri.parse("content://mms-sms/complete-conversations"
        Cursor cursor = cr.query(Telephony.Sms.Inbox.CONTENT_URI,
                fields,
                null,
                null,
                Telephony.Sms.Inbox.DEFAULT_SORT_ORDER); // Default sort order

        int totalSMS = cursor.getCount();

        Timber.d("Content resolver found %d SMS in inbox", totalSMS);

        if (cursor.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                SMS objSms = new SMS();

                // https://stackoverflow.com/questions/1976252/how-to-use-sms-content-provider-where-are-the-docs
                objSms.setId(cursor.getString(0));
                objSms.setAddress(cursor.getString(1));
                objSms.setMsg(cursor.getString(2));
                objSms.setTime(cursor.getString(3));

                allSMS.add(objSms);
                cursor.moveToNext();
            }
        }

        cursor.close();
        return allSMS;
    }

    private void getConversations(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Telephony.Sms.Conversations.CONTENT_URI, // Official CONTENT_URI from docs
                new String[]{Telephony.Sms.Conversations.MESSAGE_COUNT, Telephony.Sms.Conversations.THREAD_ID, Telephony.Sms.Conversations.SNIPPET}, // Select body text
                null,
                null,
                Telephony.Sms.Inbox.DEFAULT_SORT_ORDER); // Default sort order

        int totalConvs = cursor.getCount();
        Timber.d("Content resolver found %d SMS conversations:", totalConvs);

        if (cursor.moveToFirst()) {
            for (int i = 0; i < totalConvs; i++) {
                Timber.d("Content resolver %s messages in thread %s. Snippet: %s", cursor.getString(0), cursor.getString(1), cursor.getString(2));
                cursor.moveToNext();
            }
        }
    }
}
