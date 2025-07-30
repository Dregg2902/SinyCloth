package com.android.projectandroid.shipper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.android.projectandroid.R;
import com.android.projectandroid.shipper.NotificationItem;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationItem> notificationList;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTimestamp.setText(notification.getTimestamp());

        // Thiết lập icon và màu sắc dựa trên trạng thái
        if (notification.isSuccess()) {
            holder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_circle));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.success_green));
        } else {
            holder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cancel));
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.error_red));
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateNotifications(List<NotificationItem> newNotifications) {
        this.notificationList = newNotifications;
        notifyDataSetChanged();
    }

    public void addNotification(NotificationItem notification) {
        notificationList.add(0, notification); // Thêm vào đầu danh sách
        notifyItemInserted(0);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTimestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTimestamp = itemView.findViewById(R.id.tvNotificationTimestamp);
        }
    }
}
