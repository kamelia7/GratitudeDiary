package com.myapp.gratitudediary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.myapp.gratitudediary.DayActivity.EXTRA_RECORD_TEXT;
import static com.myapp.gratitudediary.DayActivity.EXTRA_TEXT_TO_EDIT;

public class GratitudeActivity extends AppCompatActivity {

    EditText etInput;
    Button btnSaveRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gratitude);

        etInput = findViewById(R.id.etInput);
        btnSaveRecord = findViewById(R.id.btnSaveRecord);

        etInput.requestFocus();

        Intent intent = getIntent(); //Получаем интент, который вызвал это активити
        if (intent.hasExtra(EXTRA_TEXT_TO_EDIT)) { //Если активити открыли для редактирования записи
            String text = intent.getStringExtra(EXTRA_TEXT_TO_EDIT);
            etInput.setText(text + " ");
            //устанавливаем курсор в конец текста
            etInput.setSelection(etInput.getText().length());
        }

        btnSaveRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RECORD_TEXT, etInput.getText().toString());
                setResult(RESULT_OK, intent);
                finish(); //завершаем работу GratitudeActivity, чтобы результат ушел в DayActivity
            }
        });
    }
}
