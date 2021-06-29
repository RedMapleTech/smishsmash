package tech.redmaple.smishsmash.views;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Locale;

import tech.redmaple.smishsmash.R;
import tech.redmaple.smishsmash.SmishSmash;
import tech.redmaple.smishsmash.analysis.AnalysedMessage;
import tech.redmaple.smishsmash.analysis.Messages;
import tech.redmaple.smishsmash.sms.Forward;
import timber.log.Timber;

import static android.content.Context.CLIPBOARD_SERVICE;

public class RecyclerViewFragment extends Fragment{
    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";

    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected AnalysedMessageAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected static AnalysedMessage[] analysedMessages;
    private SwipeRefreshLayout swipeContainer;

    private static TextView messageCountTextView;
    private static TextView urlCountTextView;
    private static TextView maliciousCountTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.d("RecyclerViewFragment onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;

        try {
            position = mAdapter.getPosition();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case AnalysedMessageAdapter.REPORT:
                Timber.d("Report item %d", position);
                reportMessage(position);
                break;
            case AnalysedMessageAdapter.FORWARD:
                Timber.d("Forward item %d", position);
                forwardMessage(position);
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void reportMessage(int position) {

        // get confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Report this message as spam?").setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    Timber.d("Got confirmation");

                    // copy number to clipboard
                    Context context = SmishSmash.getAppContext();
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Sender", analysedMessages[position].getMessage().getAddress());
                    clipboard.setPrimaryClip(clip);

                    CharSequence text = "Sender's number copied to clipboard";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    // send message
                    Forward.forwardSpamSmsMessage(analysedMessages[position].getMessage().getMsg(), getActivity());
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void forwardMessage(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Forward this message?").setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    Timber.d("Got confirmation");

                    // send message
                    String message = String.format("Forwarded Message (from %s): ", analysedMessages[position].getMessage().getAddress());
                    message = message.concat("\"").concat((String) analysedMessages[position].getMessage().getMsg()).concat("\"");
                    Forward.composeSmsMessage(message, getActivity());
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("RecyclerViewFragment onCreateView");

        View rootView = inflater.inflate(R.layout.recycler_fragment, container, false);
        rootView.setTag(TAG);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());

        registerForContextMenu(mRecyclerView);

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState.getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new AnalysedMessageAdapter();
        mRecyclerView.setAdapter(mAdapter);

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);

        messageCountTextView = rootView.findViewById(R.id.messageCountTextView);
        urlCountTextView = rootView.findViewById(R.id.urlCountTextView);
        maliciousCountTextView = rootView.findViewById(R.id.maliciousCountTextView);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Timber.d("onRefresh");
                refreshView();
            }
        });

        refreshView();

        return rootView;
    }


    public static void refreshCounts(int analysedCount, int messageCount, int URLCount, int maliciousCount) {
        messageCountTextView.setText(String.format(Locale.getDefault(), "%d of %d messages", analysedCount, messageCount));
        urlCountTextView.setText(String.format(Locale.getDefault(), "%d of %d messages", URLCount, analysedCount));
        maliciousCountTextView.setText(String.format(Locale.getDefault(), "%d of %d messages", maliciousCount, analysedCount));

        messageCountTextView.refreshDrawableState();
        urlCountTextView.refreshDrawableState();
        maliciousCountTextView.refreshDrawableState();
    }

    public void refreshView() {
        // Initialize data - inspect messages
        Context context = SmishSmash.getAppContext();

        swipeContainer.setRefreshing(true);
        initDataset(context);
        swipeContainer.setRefreshing(false);
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset(Context context) {
        Timber.d("RecyclerViewFragment initDataset");
        analysedMessages = Messages.getMessages(context);
        mAdapter.addAll(analysedMessages);
    }

    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }
}
