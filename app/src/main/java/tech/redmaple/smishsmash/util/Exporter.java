package tech.redmaple.smishsmash.util;

import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;

import tech.redmaple.smishsmash.sms.Inbox;
import tech.redmaple.smishsmash.sms.SMS;
import timber.log.Timber;

public class Exporter {

    public static void exportMessages(Context context, View parentLayout) {
        // get all messages
        List<SMS> msgs = Inbox.getAllSMSFromContentResolverLegacy(context);
        Timber.d("exportMessages: got %d", msgs.size());

        // setup time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        String baseDir = android.os.Environment.getExternalStorageDirectory().getPath();
        String fileName = String.format("SMS_Messages_%s.csv", dateFormat.format(Calendar.getInstance().getTime()));
        String filePath = String.format("%s%sDownload%s%s", baseDir, File.separator, File.separator, fileName);

        File f = new File(filePath);
        CSVWriter writer = null;

        try {
            if(f.exists()&&!f.isDirectory())
            {
                FileWriter fileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(fileWriter);
            }
            else {
                writer = new CSVWriter(new FileWriter(filePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (SMS sms : msgs) {
            if (writer != null) {
                Matcher urlMatcher = android.util.Patterns.WEB_URL.matcher(sms.getMsg());

                if (urlMatcher.find()) {
                    String[] data = {dateFormat.format(Long.parseLong(sms.getTime())),sms.getAddress(),String.format("\"%s\"", sms.getMsg())};
                    writer.writeNext(data);
                }
            }
        }

        if (writer != null) {
            try {
                writer.close();
                Snackbar sb = Snackbar.make(parentLayout, String.format("Messages exported to \"Download/%s\".", fileName), Snackbar.LENGTH_SHORT);
                sb.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
