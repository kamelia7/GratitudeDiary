package com.myapp.gratitudediary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Gratitude> gratitudes = new ArrayList<>();
    final GratitudeAdapter gratitudeAdapter = new GratitudeAdapter(gratitudes);
    FloatingActionButton fabAddRecord;
    CoordinatorLayout clContainer;

    private DB db;

    static final int ADD_GRATITUDE_REQUEST = 0;
    static final int EDIT_GRATITUDE_REQUEST = 1;

    private int editedGratitudePosition;
    private long editedGratitudeId;

    private Gratitude recentlyDeletedGratitude;
    private int recentlyDeletedGratitudePosition;

    public static final String EXTRA_RECORD_TEXT = "record_text";
    public static final String EXTRA_TEXT_TO_EDIT = "text_to_edit";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //должен быть вызван
        if (data == null) return; //data будет null, если передать resultCode, а интент не передать

        if (resultCode == RESULT_OK && data.hasExtra(EXTRA_RECORD_TEXT)) {
            String recordText = data.getStringExtra(EXTRA_RECORD_TEXT);
            switch (requestCode) {

                case ADD_GRATITUDE_REQUEST:
                    //новые записи добавляются в базу данных в обычном порядке на последнюю позицию,
                    //а в RecyclerView вставляются на 0ю позицию (недавние записи будут вверху списка)
                    long recordId = db.addRecord(recordText);
                    gratitudes.add(0, new Gratitude(recordId, recordText));
                    gratitudeAdapter.notifyItemInserted(0); //так появляется анимация
                    break;

                case EDIT_GRATITUDE_REQUEST:
                    db.updateRecord(editedGratitudeId, recordText);
                    gratitudes.set(editedGratitudePosition, new Gratitude(editedGratitudeId, recordText));
                    gratitudeAdapter.notifyItemChanged(editedGratitudePosition);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clContainer = findViewById(R.id.clContainer);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setAdapter(gratitudeAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(new SwipeToDeleteCallback.OnGratitudeDeleteListener() {
            @Override
            public void onGratitudeDelete(int position) {
                recentlyDeletedGratitudePosition = position;         //сохраним позицию только что удаленного эл-та
                recentlyDeletedGratitude = gratitudes.get(position); //сохраним удаленный элемент в переменной-члене класса
                db.deleteRecord(recentlyDeletedGratitude.getId());   //удаляем эл-т из бд
                gratitudes.remove(position);                         //удаляем эл-т из списка
                gratitudeAdapter.notifyItemRemoved(position);        //обновляем адаптер
                showUndoSnackbar();                                  //показать снекбар отмены удаления
            }
        }));

        itemTouchHelper.attachToRecyclerView(recyclerView);

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
                intent.putExtra(EXTRA_TEXT_TO_EDIT, gratitude.getText());
                editedGratitudePosition = position;
                editedGratitudeId = gratitude.getId();
                startActivityForResult(intent, EDIT_GRATITUDE_REQUEST);
            }
        });

        //Открываем подключение к БД (или создаем БД, если она не создана, и подключаемся к ней)
        db = new DB(this);
        db.open();

        readGratitudesFromDBInReverseOrder(); //при 1м запуске (когда БД еще пустая) ничего не вернется

        if (gratitudes.isEmpty()) { //из БД ничего не прочли, она пустая - 1й запуск
            for (Gratitude gratitude : createGratitudeList()) //заполняем БД сторонними данными, а не из листа gratitudes
                db.addRecord(gratitude.getText());
            readGratitudesFromDBInReverseOrder(); //читаем данные из только что заполенной БД в лист gratitudes
        }
    }

    private List<Gratitude> createGratitudeList() {
        List<Gratitude> localList = new ArrayList<>();
        localList.add(new Gratitude(1, "Благодарю за..."));
        localList.add(new Gratitude(2, "Благодарю за..."));
        localList.add(new Gratitude(3,"Благодарю за небо над головой и солнышко"));
        return localList;
    }

    private void readGratitudesFromDB() {
        gratitudes.addAll(db.getAllGratitudes()); //не меняем ссылку на лист, чтобы не баговал связанный с ним адаптер
        gratitudeAdapter.notifyDataSetChanged();
        // Чтобы прочитать и вывести на экран данные из БД, их надо занести в лист, связанный с адаптером.
        // И затем перерисовать все эл-ты списка RecyclerView.
        //(На этом моменте записи из БД преобразуются в объекты типа Gratitude)
    }

    private void readGratitudesFromDBInReverseOrder() {
        List<Gratitude> list = new ArrayList<>();
        //читаем данные из БД в обычном порядке, затем реверсим этот список и выводим его на экран (недавние записи будут вверху списка)
        list.addAll(db.getAllGratitudes());
        gratitudes.addAll(Utils.getReverseList(list));
        gratitudeAdapter.notifyDataSetChanged();
    }

    private void showUndoSnackbar() {
        Snackbar.make(clContainer, R.string.snackbar_text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoDelete();
                    }
                })
        .show();
    }

    private void undoDelete() {
        //возвращаем запись не в конец бд с новым id, а с прошлым id на старое место (бд автоматически сортирует записи по возрастанию id)
        db.addRecord(recentlyDeletedGratitude.getText(), recentlyDeletedGratitude.getId());
        gratitudes.add(recentlyDeletedGratitudePosition, recentlyDeletedGratitude);
        gratitudeAdapter.notifyItemInserted(recentlyDeletedGratitudePosition);
    }
}
