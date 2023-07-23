package com.example.smartcitytravel.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcitytravel.DataModel.Message;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    static final int RECEIVE_MESSAGE_TYPE = 0;
    static final int SENDER_MESSAGE_TYPE = 1;
    private Context context;
    private ArrayList<Message> messageList;
    private PreferenceHandler preferenceHandler;
    private User user;
    private Util util;
    private RelativeLayout errorLayout;

    public ChatAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;

        preferenceHandler = new PreferenceHandler();
        user = preferenceHandler.getLoggedInAccountPreference(context);
        util = new Util();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_MESSAGE_TYPE) {
            return (new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_send, parent, false)));
        } else {
            return (new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_receive, parent, false)));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.userNameTxt.setText(util.capitalizedName(messageList.get(position).getSenderName()));
        holder.messageTxt.setText(messageList.get(position).getMessage());

        String time12HourFormat = formatTimeInto12HourFormat(messageList.get(position).getTime());
        holder.timeTxt.setText(time12HourFormat);

        if (SENDER_MESSAGE_TYPE == holder.getItemViewType()
                && messageList.get(position).isError()) {
            errorLayout = holder.itemView.findViewById(R.id.errorLayout);
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    //add new messages in old message list
    public void addData(ArrayList<Message> newMessageList) {
        int insertPosition = this.messageList.size();
        this.messageList.addAll(newMessageList);
        notifyItemChanged(insertPosition);
    }

    //add one new message in old message list
    public void addSingleData(Message newMessage) {
        int insertPosition = this.messageList.size();
        this.messageList.add(newMessage);
        notifyItemChanged(insertPosition);
    }

    //remove all error messages which are unable to send to database
    public void removeErrorData() {
        for (int index = 0; index < messageList.size(); index++) {
            if (messageList.get(index).isError()) {
                messageList.remove(index);
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<Message> getData() {
        return messageList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageTxt, userNameTxt, timeTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.userNameTxt);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (messageList.get(position).getSenderID().equals(user
                .getUserId())) {
            return SENDER_MESSAGE_TYPE;
        } else {
            return RECEIVE_MESSAGE_TYPE;

        }
    }

    // 24 hours time into 12 Am/PM
    public String formatTimeInto12HourFormat(String time) {
        try {
            SimpleDateFormat time24Format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date datetime = time24Format.parse(time);
            SimpleDateFormat time12Format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return time12Format.format(datetime);
        } catch (ParseException ignored) {
            return null;
        }

    }
}
