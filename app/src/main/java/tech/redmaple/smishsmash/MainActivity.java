package tech.redmaple.smishsmash;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

import tech.redmaple.smishsmash.views.RecyclerViewFragment;

public class MainActivity extends AppCompatActivity {

    private static final int READ_SMS_PERMISSION_CODE = 100;
    private static final int SEND_SMS_PERMISSION_CODE = 101;
    private static final int CONTACT_PERMISSION_CODE = 102;
    private static final int INTERNET_PERMISSION_CODE = 103;
    private static final int MULTIPLE_CODE = 99;

    private static final String ABOUT_TEXT = "<html><h4>About</h4>This is a Proof-of-Concept application from Red Maple Technologies." +
            "<br><br>" +
            "<h4>How it Works</h4>SmishSmash retrieves all messages currently in the inbox, and inspects them for URLs (links)." +
            " If it can't recognise the link, and the message contains keywords associated with a known company, it flags it as malicious." +
            "This approach isn't foolproof, but works against all the spam messages we have collected so far." +
            "<br><br>" +
            "<h4>What to do if you receive a phishing message</h4>" +
            "Forward it to the UK national spam reporting number, 7726. This works for all UK mobile networks." +
            "You can do this by long-pressing on a message in this app and selecting \"Report\"." +
            "<br><br>" +
            "<h4>What would be better</h4>" +
            "We built this to show that it is possible to detect malicious messages with reasonable accuracy. " +
            "A better solution would be for the mobile operators to inspect messages that contain links, " +
            "although this would mean them inspecting the content of all messages." +
            "<br><br>" +
            "<h4>More Information</h4>For information on smishing, see:" +
            "<ul><li><a href=\"https://redmaple.tech/blogs/smish-smash/\"> Our Blog</a></li>" +
            "<li><a href=\"https://www.ncsc.gov.uk/guidance/suspicious-email-actions\"> This NCSC page</a></li>" +
            "<li><a href=\"https://www.which.co.uk/consumer-rights/advice/how-to-deal-with-spam-text-messages-axsG54B0mH0H\"> This Which? page</a><li>" +
            "<li><a href=\"https://www.actionfraud.police.uk/report-phishing\"> This ActionFraud page</a></ul>" +
            "<br>" +
            "<h4>About Us</h4>Read more at <a href=\"https://redmaple.tech\">redmaple.tech.</html>";

    private RecyclerViewFragment fragment;

    private static boolean ready = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Timber.d("Setup done, checking permissions");
        ready = checkPermissions();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new RecyclerViewFragment();
            transaction.replace(R.id.main_fragment, fragment);
            transaction.commit();

            if (ready) {
                Timber.d("Running automatically");
            } else {
                Timber.d("Tried to run automatically, but need permissions");
            }
        }
    }


    // Function to check and request permission.
    public boolean checkPermissions()
    {
        boolean okay = true;
        
        List<String> needed = new ArrayList<>();
        int currentCode = MULTIPLE_CODE;

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            Timber.d("Lacking permission: READ_SMS");
            needed.add(Manifest.permission.READ_SMS);
            currentCode = READ_SMS_PERMISSION_CODE;
            okay = false;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            needed.add(Manifest.permission.SEND_SMS);
            Timber.d("Lacking permission: SEND_SMS");
            currentCode = SEND_SMS_PERMISSION_CODE;
            okay = false;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            needed.add(Manifest.permission.READ_CONTACTS);
            Timber.d("Lacking permission: READ_CONTACTS");
            currentCode = CONTACT_PERMISSION_CODE;
            okay = false;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            needed.add(Manifest.permission.INTERNET);
            Timber.d("Lacking permission: INTERNET");
            currentCode = INTERNET_PERMISSION_CODE;
            okay = false;
        }

        if (needed.size() > 2) {
            ActivityCompat.requestPermissions(MainActivity.this, needed.toArray(new String[]{}), MULTIPLE_CODE);
        } else if (needed.size() == 1) {
            ActivityCompat.requestPermissions(MainActivity.this, needed.toArray(new String[]{}), currentCode);
        }

        return okay;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MULTIPLE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Multiple Permissions Granted",
                        Toast.LENGTH_SHORT)
                        .show();

                ready = true;
            } else {
                Toast.makeText(MainActivity.this,
                        "Some Permissions Denied. Need them all to run.",
                        Toast.LENGTH_SHORT)
                        .show();

                ready = false;
            }
        } else if (requestCode == READ_SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Read SMS Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();

                ready = true;
            } else {
                Toast.makeText(MainActivity.this,
                        "Read SMS Permission Denied. Need it to run.",
                        Toast.LENGTH_SHORT)
                        .show();

                ready = false;
            }
        } else if (requestCode == SEND_SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Send SMS Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                ready = true;
            } else {
                Toast.makeText(MainActivity.this,
                        "Send SMS Permission Denied. Need it to run.",
                        Toast.LENGTH_SHORT)
                        .show();

                ready = false;
            }
        } else if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Read Contacts Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                ready = true;
            } else {
                Toast.makeText(MainActivity.this,
                        "Read Contacts Permission Denied. Need it to run.",
                        Toast.LENGTH_SHORT)
                        .show();

                ready = false;
            }
        } else if (requestCode == INTERNET_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Internet Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                ready = true;
            } else {
                Toast.makeText(MainActivity.this,
                        "Internet Permission Denied. Need it to run.",
                        Toast.LENGTH_SHORT)
                        .show();

                ready = false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_item_about) {
            Timber.d("about_menu clicked");
            launchAboutDialog();
        } else if (id == R.id.menu_item_help) {
            Timber.d("about_menu clicked");
            launchHelpDialog();
        } else if (id == R.id.menu_item_refresh) {
            fragment.refreshView();
        } /*else if (id == R.id.menu_item_service) {
            item.setChecked(!item.isChecked());
            Timber.d("Service toggled: now %s", item.isChecked() ? "on" : "off");
            item.setTitle(item.isChecked() ? "Disable Service" : "Enable Service");
        }*/

        return super.onOptionsItemSelected(item);
    }

    private void launchHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = View.inflate(MainActivity.this, R.layout.help_view, null);
        builder.setView(view);
        builder.setPositiveButton("Okay", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void launchAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = View.inflate(MainActivity.this, R.layout.aboutview, null);
        builder.setView(view);
        builder.setPositiveButton("Okay", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();

        TextView tv = view.findViewById(R.id.aboutTextView);
        tv.setText(Html.fromHtml(ABOUT_TEXT, Html.FROM_HTML_MODE_COMPACT));
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        // add the version string
        String version = "";

        try {
            PackageInfo pInfo = SmishSmash.getAppContext().getPackageManager().getPackageInfo(SmishSmash.getAppContext().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView versionTV = view.findViewById(R.id.versionString);

        if (versionTV != null) {
            versionTV.setText(String.format("\nVersion: %s", version));
        }

        alertDialog.show();
    }

}