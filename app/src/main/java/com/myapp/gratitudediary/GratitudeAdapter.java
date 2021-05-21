package com.myapp.gratitudediary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GratitudeAdapter extends RecyclerView.Adapter<GratitudeAdapter.GratitudeViewHolder> {

    private final List<Gratitude> data;

    public GratitudeAdapter(List<Gratitude> data) {
        this.data = data;
    }

    // Создаем свой интерфейс для обработки нажатия,
    // поскольку хотим реализовать обработку нажатия в классе DayActivity, исходя из того кода,
    // который там имеется (или из какого-либо другого класса по необходимости)
    public interface OnGratitudeClickListener {
        void onGratitudeClick(Gratitude gratitude, int position);
    }

    //поле не final, т.к. делаем сеттер, а не задаем значение в конструкторе
    private OnGratitudeClickListener onGratitudeClickListener;

    public void setOnGratitudeClickListener(OnGratitudeClickListener onGratitudeClickListener) {
        this.onGratitudeClickListener = onGratitudeClickListener;
    }

    @NonNull
    @Override
    public GratitudeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_record, parent, false);
        return new GratitudeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GratitudeViewHolder holder, int position) {
        Gratitude gratitude = data.get(position);
        holder.setGratitude(gratitude);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //класс холдера не static для возможности использования в нем нестатичного поля onGratitudeClickListener
    class GratitudeViewHolder extends RecyclerView.ViewHolder {

        TextView tvText;

        public GratitudeViewHolder(@NonNull final View itemView) {  //final добивила
            super(itemView);

            tvText = itemView.findViewById(R.id.tvText);

            itemView.setOnClickListener(new View.OnClickListener() { //вешаем лисенер не на tvText, а на itemView
                @Override
                public void onClick(View v) {
                    //создан onGratitudeClickListener, чтобы не тащить в адаптер DayActivity,
                    // а только из класса активити можем вызвать другое активити
                    if (onGratitudeClickListener != null) {
                        int position = GratitudeViewHolder.this.getAdapterPosition();
                        onGratitudeClickListener.onGratitudeClick(data.get(position), position);
                    }
                }
            });
        }

        private void setGratitude(Gratitude gratitude) {
            tvText.setText(gratitude.getText());
        }
    }
}
