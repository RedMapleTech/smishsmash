package tech.redmaple.smishsmash.contacts;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class Lookup {
    public static final String NOT_A_CONTACT = "BLAH";

    /**
     * Lookup whether phone number is an existing contact
     * Stolen from https://stackoverflow.com/questions/3505865/android-check-phone-number-present-in-contact-list-phone-number-retrieve-fr
     * @param context for app
     * @param number to lookup
     * @return boolean
     */
    public static boolean contactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };

        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);

        try {
            if (cur.moveToFirst()) {
                cur.close();
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public static String contactDetails(Context context, String number) {
        String name = NOT_A_CONTACT;

        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };

        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);

        try {
            if (cur.moveToFirst()) {
                name = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cur.close();
            }
        } finally {
            if (cur != null)
                cur.close();
        }

        return name;
    }
}
