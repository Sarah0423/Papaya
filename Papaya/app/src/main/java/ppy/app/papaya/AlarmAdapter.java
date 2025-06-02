package ppy.app.papaya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;

    public AlarmAdapter(List<Alarm> alarmList) {
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);
        holder.tvAlarmName.setText(alarm.getAlarmsType());
        holder.tvAlarmInfo.setText(alarm.getAlarmsInfo());
        int imgResId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(alarm.getAlarmsPhoto(), "mipmap", holder.itemView.getContext().getPackageName());
        holder.ivAlarmImg.setImageResource(imgResId);
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAlarmImg;
        TextView tvAlarmName, tvAlarmInfo;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAlarmImg = itemView.findViewById(R.id.iv_alarm_img);
            tvAlarmName = itemView.findViewById(R.id.tv_alarm_name);
            tvAlarmInfo = itemView.findViewById(R.id.tv_alarm_info);
        }
    }
}
