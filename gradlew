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
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Gratitude> gratitudes = new ArrayList<>();
    final GratitudeAdapter gratitudeAdapter = new GratitudeAdapter(gratitudes);
    FloatingActionButton fabAddRecord;

    static final int ADD_GRATITUDE_REQUEST = 0;
    static final int EDIT_GRATITUDE_REQUEST = 1;

    private int editedGratitudePos;

    public static final String EXTRA_RECORD_TEXT = "record_text";
    public static final String EXTRA_TEXT_TO_EDIT = "text_to_edit";

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
        super.onActivityResult(requestCode, resultCode, data); //должен быть
        if (data == null) return; //это если передать резалт код, а интент не передать

        if (resultCode == RESULT_OK && data.hasExtra(EXTRA_RECORD_TEXT)) { //в private static final String EXTRA_RECORD_TEXT
            String recordText = data.getStringExtra(EXTRA_RECORD_TEXT);
            switch (requestCode) {
                case ADD_GRATITUDE_REQUEST:
                    gratitudes.add(0, new Gratitude(recordText));
                    gratitudeAdapter.notifyItemInserted(0); //так появляется анимация
                    break;

                case EDIT_GRATITUDE_REQUEST:
                    gratitudes.set(editedGratitudePos, new Gratitude(recordText));
                    gratitudeAdapter.notifyItemChanged(editedGratitudePos);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

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
                startActivityForResult(intent, ADD_GRATITUDE_REQUEST);
            }
        });

        gratitudeAdapter.setOnGratitudeClickListener(new GratitudeAdapter.OnGratitudeClickListener() {
            @Override
            public void onGratitudeClick(Gratitude gratitude, int position) {

                Intent intent = new Intent(MainActivity.this, AddingGratitudeActivity.class);
                intent.putExtra(EXTRA_TEXT_TO_EDIT, gratitude.getText()); //или к БД тут можно обратиться
                editedGratitudePos = position;
                startActivityForResult(intent, EDIT_GRATITUDE_REQUEST);
            }
        });
    }

    private List<Gratitude> createGratitudeList() {
        List<Gratitude> localList = new ArrayList<>();
        localList.add(new Gratitude("Благодарю за небо над головой и солнышко"));
        localList.add(new Gratitude("Благодарю за..."));
        localList.add(new Gratitude("Благодарю за..."));
        return localList;
    }
}
                                            