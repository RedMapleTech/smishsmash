package tech.redmaple.smishsmash.sms;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Forward {

    private static final String SPAM_NUMBER = "7726";

    // https://developer.android.com/guide/components/intents-common.html#Messaging
    public static void composeSmsMessage(String content, Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("smsto:"));
        intent.putExtra("sms_body", content);
        intent.putExtra("sms_to", SPAM_NUMBER);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    // https://developer.android.com/guide/components/intents-common.html#Messaging
    public static void forwardSpamSmsMessage(String content, Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String toURI = String.format("smsto:%s", SPAM_NUMBER);

        intent.setData(Uri.parse(toURI));
        intent.putExtra("sms_body", content);
        intent.putExtra("sms_to", SPAM_NUMBER);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
