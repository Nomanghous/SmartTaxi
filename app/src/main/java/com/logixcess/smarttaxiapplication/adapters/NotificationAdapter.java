package com.logixcess.smarttaxiapplication.adapters;
import android.content.Context;
        import android.support.v7.widget.PopupMenu;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        //import com.bumptech.glide.Glide;

import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    private Context mContext;
    private List<NotificationPayload> notificationPayloadList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView noti_title, noti_text;

        public MyViewHolder(View view) {
            super(view);
            noti_title = (TextView) view.findViewById(R.id.noti_title);
            noti_text = (TextView) view.findViewById(R.id.noti_text);
        }
    }


    public NotificationAdapter(Context mContext, List<NotificationPayload> albumList) {
        this.mContext = mContext;
        this.notificationPayloadList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_history_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NotificationPayload notificationPayload = notificationPayloadList.get(position);
        holder.noti_title.setText(notificationPayload.getTitle());
        holder.noti_text.setText(notificationPayload.getDescription());


    }


    @Override
    public int getItemCount() {
        return notificationPayloadList.size();
    }
}
