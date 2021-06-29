package tech.redmaple.smishsmash.views;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import tech.redmaple.smishsmash.R;
import tech.redmaple.smishsmash.SmishSmash;
import tech.redmaple.smishsmash.analysis.AnalysedMessage;
import tech.redmaple.smishsmash.analysis.OrganisationInfo;
import timber.log.Timber;

public class AnalysedMessageAdapter extends RecyclerView.Adapter<AnalysedMessageAdapter.ViewHolder> {
    private static AnalysedMessage[] messageData;

    private static OrganisationInfo[] orgsInfo;

    protected static final int REPORT = 1;
    protected static final int FORWARD = 2;

    // Add a list of items -- change to type used
    public void addAll(AnalysedMessage[] dataSet) {
        // get comparison data
        orgsInfo = OrganisationInfo.getInfoFromJSON(SmishSmash.getAppContext());

        // get data
        messageData = dataSet;
        notifyDataSetChanged();
    }

    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.sms_view, viewGroup, false);

        return new ViewHolder(view);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        if (!messageData[position].isAnalysed()) {
            // kick of the analysis of the current message, which will persist whilst the app is running
            messageData[position].analyse(SmishSmash.getAppContext(), orgsInfo);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(Calendar.getInstance().getTimeZone());

        // set the text
        viewHolder.getMessageTextView().setText(String.format("\"%s\"", messageData[position].getMessage().getMsg()));

        if (messageData[position].getAnalysis() == null) {
            viewHolder.getAnalysisTextView().setVisibility(View.GONE);
        } else {
            viewHolder.getAnalysisTextView().setText(String.format("%s", messageData[position].getAnalysis()));
        }

        if (messageData[position].hasLink()) {
            viewHolder.getLinkTextView().setText(combineLinks(messageData[position].getLinks()));
            viewHolder.getLinkTextView().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getLinkTextView().setVisibility(View.GONE);
        }

        viewHolder.getMessageSender().setText(String.format("From: %s", messageData[position].getMessage().getAddress()));

        String msgTime = dateFormat.format(Long.parseLong(messageData[position].getMessage().getTime()));
        viewHolder.getMessageTimeTextView().setText(msgTime);

        // set the icons
        // recycled views mean we need to set them either way every time
        if (messageData[position].hasLink()) {
            viewHolder.getLinkView().setVisibility(View.VISIBLE);
            viewHolder.getAnalysisTextView().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getLinkView().setVisibility(View.GONE);
            viewHolder.getAnalysisTextView().setVisibility(View.GONE);
        }

        // if malicious show that icon
        if (messageData[position].isMalicious()) {
            viewHolder.getVirusView().setVisibility(View.VISIBLE);
            viewHolder.getTickView().setVisibility(View.GONE);
            viewHolder.getQuestionView().setVisibility(View.GONE);
            viewHolder.getAnalysisTextView().setTextAppearance(R.style.warningStyleText);
        }
        // if not malicious but has a URL we couldn't check show the question mark
        else if (messageData[position].hasUnknownLink()) {
            viewHolder.getVirusView().setVisibility(View.GONE);
            viewHolder.getTickView().setVisibility(View.GONE);
            viewHolder.getQuestionView().setVisibility(View.VISIBLE);
            viewHolder.getAnalysisTextView().setTextAppearance(R.style.unsureStyleText);
        }
        // else okay so show the tick
        else {
            viewHolder.getVirusView().setVisibility(View.GONE);
            viewHolder.getTickView().setVisibility(View.VISIBLE);
            viewHolder.getQuestionView().setVisibility(View.GONE);
            viewHolder.getAnalysisTextView().setTextAppearance(R.style.defaultText);
        }

        // if it's a contact show the icon
        if (messageData[position].isCompanySender()) {
            viewHolder.getCompanyView().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getCompanyView().setVisibility(View.GONE);
        }

        if (messageData[position].isContact()) {
            viewHolder.getContactView().setVisibility(View.VISIBLE);
            viewHolder.getMessageSender().setText(String.format("From: %s", messageData[position].getContactName()));
        } else {
            viewHolder.getContactView().setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(viewHolder.getAdapterPosition());
                Timber.d("Viewholder position: %s", viewHolder.getAdapterPosition());
                return false;
            }
        });

        updateCounts();
    }

    private void updateCounts() {
        int messageCount = messageData.length;
        int analysedCount = 0;
        int URLCount = 0;
        int maliciousCount = 0;

        // update counts
        for (AnalysedMessage msg : messageData) {
            if (msg.isAnalysed())
            {
                analysedCount++;

                if (msg.hasLink()) {
                    URLCount++;

                    if (msg.isMalicious()) {
                        maliciousCount++;
                    }
                }
            }
        }

        Timber.d("Updating counts");

        // update counts in the fragment view
        RecyclerViewFragment.refreshCounts(analysedCount, messageCount, URLCount, maliciousCount);
    }

    private String combineLinks(String[] links) {
        StringBuilder sb = new StringBuilder();
        sb.append(links.length == 1 ? "Extracted Link: " : "Extracted Links: ");

        for (int i = 0; i < links.length; i++) {

            sb.append(links[i]);

            if (i < links.length - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (messageData != null) {
            return messageData.length;
        }

        return 0;
    }


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private final TextView msgTextView;
        private final TextView analysisTextView;
        private final TextView messageTimeTextView;
        private final TextView messageSender;
        private final TextView linkTextView;

        private final ImageView virusView;
        private final ImageView linkView;
        private final ImageView contactView;
        private final ImageView companyView;
        private final ImageView tickView;
        private final ImageView questionView;

        public ViewHolder(View view) {
            super(view);

            // set ourselves as the listener
            view.setOnCreateContextMenuListener(this);

            // instantiate objects
            msgTextView = view.findViewById(R.id.messageTextView);
            analysisTextView = view.findViewById(R.id.analysisTextView);
            messageTimeTextView = view.findViewById(R.id.messageTime);
            messageSender = view.findViewById(R.id.messageSender);
            linkTextView = view.findViewById(R.id.linksTextView);

            virusView = view.findViewById(R.id.virusIcon);
            questionView = view.findViewById(R.id.questionIcon);
            linkView = view.findViewById(R.id.linkIcon);
            contactView = view.findViewById(R.id.contactIcon);
            companyView = view.findViewById(R.id.companyIcon);
            tickView = view.findViewById(R.id.tickIcon);
        }

        public ImageView getCompanyView() {
            return companyView;
        }

        public TextView getLinkTextView() {
            return linkTextView;
        }

        public ImageView getTickView() {
            return tickView;
        }

        public ImageView getQuestionView() {
            return questionView;
        }

        public TextView getMessageSender() {
            return messageSender;
        }

        public TextView getMessageTimeTextView() {
            return messageTimeTextView;
        }

        public ImageView getContactView() {
            return contactView;
        }

        public ImageView getVirusView() {
            return virusView;
        }

        public ImageView getLinkView() {
            return linkView;
        }

        public TextView getMessageTextView() {
            return msgTextView;
        }

        public TextView getAnalysisTextView() {
            return analysisTextView;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            //groupId, itemId, order, title
            menu.add(Menu.NONE, REPORT, 1, "Report as Spam");
            menu.add(Menu.NONE, FORWARD, 2, "Forward Message");
        }
    }
}
