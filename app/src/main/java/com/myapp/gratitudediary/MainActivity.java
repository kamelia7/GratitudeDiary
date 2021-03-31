package com.myapp.gratitudediary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Gratitude> gratitudes = new ArrayList<>();
    final GratitudeAdapter gratitudeAdapter = new GratitudeAdapter(gratitudes);
    FloatingActionButton fabAddRecord;

    // onActivityResult вызывается, когда закрывается AddingGratitudeActivity,
    // тем самым давая нам знать, что закрылось Activity,
    // которое мы вызывали методом startActivityForResult.

    //В MainActivity за прием результатов с вызванных Activity отвечает метод onActivityResult.
    // В нем мы распаковали Intent и .??...............................

    //requestCode – тот же идентификатор, что и в startActivityForResult.
    // По нему определяем, с какого Activity пришел результат.
    //resultCode – код возврата. Определяет, успешно прошел вызов или нет.
    //data – Intent, в котором возвращаются данные
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        } //todo задействовать request и resultcode
        String recordText = data.getStringExtra("record_text");
        gratitudes.add(new Gratitude(recordText));
        gratitudeAdapter.notifyDataSetChanged();//TODO это переделать. сделать лисенер для связи адаптера и UI
        //gratitudeAdapter.notifyItemInserted();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Делаем так, чтобы было видно, что поле меняется именно тут. А не где-то во внутренних фун-х
        gratitudes.addAll(createGratitudeList());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setAdapter(gratitudeAdapter);

        fabAddRecord = findViewById(R.id.fabAddRecord);

        fabAddRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddingGratitudeActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private List<Gratitude> createGratitudeList() {
        List<Gratitude> localList = new ArrayList<>();
        localList.add(new Gratitude("Я благодарна за небо над головой и солнышко"));
        localList.add(new Gratitude("Я благодарна..."));
        localList.add(new Gratitude("Я благодарна..."));
        return localList;
    }
}
