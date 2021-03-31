package com.myapp.gratitudediary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddingGratitudeActivity extends AppCompatActivity {

    EditText etInput;
    Button btnSaveRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_gratitude);

        etInput = findViewById(R.id.etInput);
        btnSaveRecord = findViewById(R.id.btnSaveRecord);

        etInput.requestFocus(); //чтобы курсор сразу был на строке ввода
        //etInput.setSelection(0);

        //если открыли на редактирование
        //Получаем интент, который вызвал это активити
        Intent intent = getIntent();
        if (intent.hasExtra("text_to_edit")) {
            String text = intent.getStringExtra("text_to_edit");
            etInput.setText(text);
        }

        btnSaveRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // если будем получать в этом интенте текст для редактирования
                // получаем Intent, который вызывал это Activity
                //Intent intent = getIntent();

                //Данные передаются и возвращаются в РАЗНЫХ интентах.
                //У каждого активити свой интент!!!!!!!!!!!!!!!!!!!!!!!!!!!

                //нужно это
                Intent intent = new Intent();
                if (v.getId() == R.id.btnSaveRecord) { //TODO возможно потом тут будет switch
                    intent.putExtra("record_text", etInput.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }

                //Обратите внимание, мы никак не адресуем этот Intent.
                // Т.е. ни класс, ни action мы не указываем.
                // И получается, что непонятно куда пойдет этот Intent.
                // Но метод setResult знает, куда надо вернуть Intent - в «родительское» Activity,
                // которое выполнило вызов метода startActivityForResult.
                // Также в setResult мы передаем константу RESULT_OK,
                // означающую успешное завершение вызова.
                // И именно она передастся в параметр resultCode метода onActivityResult в MainActivity.java.
                // Далее методом finish мы завершаем работу AddingGratitudeActivity,
                // чтобы результат ушел в MainActivity.
            }
        });
    }
}