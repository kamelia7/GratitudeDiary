package com.myapp.gratitudediary;

import android.content.Context;
import android.content.Intent;
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

    @NonNull
    @Override
    public GratitudeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_record, parent, false);
        return new GratitudeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GratitudeViewHolder holder, int position) {
        //holder.setRecordText(); //было. вар 1
        holder.setRecordText(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //interface ShowUndoMessageListener {
    //    void onMessageShowed(Context context);
    //}


    class GratitudeViewHolder extends RecyclerView.ViewHolder {

        TextView tvText;

        public GratitudeViewHolder(@NonNull final View itemView) {  //final добивила
            super(itemView);

            tvText = itemView.findViewById(R.id.tvText);

            tvText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO сделать это с лисенером, чтобы не тащить в адаптер MainActivity
                    //Intent intent = new Intent(MainActivity.this, AddingGratitudeActivity.class);
                    //intent.putExtra("text_to_edit", tvText.getText().toString());
                    //startActivity(intent); //startActivityForResult?

                    //String text = tvText.getText().toString();
                    return false; //надо true?
                }
            });
        }

        //или лучше public?
        private void setRecordText(int position) {
            //int position = getAdapterPosition();  //вар 1
            Gratitude gratitude = data.get(position);
            tvText.setText(gratitude.getText());
        }
    }

}
