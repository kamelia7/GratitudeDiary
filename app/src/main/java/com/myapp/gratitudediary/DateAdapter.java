package com.myapp.gratitudediary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private final List<Long> data;

    public DateAdapter(List<Long> data) {
        this.data = data;
    }

    public interface OnDateClickListener {
        void onDateClick(long date, int position);
    }

    private OnDateClickListener onDateClickListener;

    public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
        this.onDateClickListener = onDateClickListener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        long date = data.get(position);
        holder.setDate(date);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfMonth;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDayOfMonth = itemView.findViewById(R.id.tvDayOfMonth);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDateClickListener != null) {
                        int position = DateViewHolder.this.getAdapterPosition();
                        onDateClickListener.onDateClick(data.get(position), position);
                    }
                }
            });
        }

        private void setDate(long date) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date);
            tvDayOfMonth.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        }
    }
}
