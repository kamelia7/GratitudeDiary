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
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DayActivity extends AppCompatActivity {

    RecyclerView rvGratitudes;
    List<Gratitude> gratitudes = new ArrayList<>();
    final GratitudeAdapter gratitudeAdapter = new GratitudeAdapter(gratitudes);
    FloatingActionButton fabAddRecord;
    CoordinatorLayout clGratitudesContainer;
    Button btnCalendar;

    private DB db;

    static final int ADD_GRATITUDE_REQUEST = 0;
    static final int EDIT_GRATITUDE_REQUEST = 1;

    private int editedGratitudePosition;
    private long editedGratitudeId;
    private long editedGratitudeCreationDate;

    private Gratitude recentlyDeletedGratitude;
    private int recentlyDeletedGratitudePosition;

    Timer timer;
    TimerTask timerTask;
    private static final int SNACKBAR_LENGTH_LONG = 2750; //Snackbar's duration LENGTH_LONG is 2750 millis

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
                    long creationDate = new Date().getTime(); //ms
                    long recordId = db.addGratitudeRecord(recordText, creationDate);
                    gratitudes.add(0, new Gratitude(recordId, recordText, creationDate));
                    gratitudeAdapter.notifyItemInserted(0); //так появляется анимация
                    break;

                case EDIT_GRATITUDE_REQUEST:
                    long editionDate = new Date().getTime();
                    db.updateGratitudeRecord(editedGratitudeId, recordText, editionDate);
                    Gratitude gratitude = new Gratitude(editedGratitudeId, recordText, editedGratitudeCreationDate);
                    gratitude.setEditionDate(editionDate);
                    gratitudes.set(editedGratitudePosition, gratitude);
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
        setContentView(R.layout.activity_day);

        clGratitudesContainer = findViewById(R.id.clGratitudesContainer);

        rvGratitudes = findViewById(R.id.rvGratitudes);
        rvGratitudes.setLayoutManager(new LinearLayoutManager(this));
        rvGratitudes.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        rvGratitudes.setAdapter(gratitudeAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(new SwipeToDeleteCallback.OnGratitudeDeleteListener() {
            @Override
            public void onGratitudeDelete(int position) {
                recentlyDeletedGratitudePosition = position;         //сохраним позицию только что удаленного эл-та
                recentlyDeletedGratitude = gratitudes.get(position); //сохраним удаленный элемент в переменной-члене класса
                gratitudes.remove(position);                         //удаляем эл-т из списка
                gratitudeAdapter.notifyItemRemoved(position);        //обновляем адаптер
                showUndoSnackbar();                                  //показать снекбар отмены удаления
            }
        }));

        itemTouchHelper.attachToRecyclerView(rvGratitudes);

        fabAddRecord = findViewById(R.id.fabAddRecord);

        fabAddRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DayActivity.this, GratitudeActivity.class);
                startActivityForResult(intent, ADD_GRATITUDE_REQUEST);
            }
        });

        gratitudeAdapter.setOnGratitudeClickListener(new GratitudeAdapter.OnGratitudeClickListener() {
            @Override
            public void onGratitudeClick(Gratitude gratitude, int position) {

                Intent intent = new Intent(DayActivity.this, GratitudeActivity.class);
                intent.putExtra(EXTRA_TEXT_TO_EDIT, gratitude.getText());
                editedGratitudePosition = position;
                editedGratitudeId = gratitude.getId();
                editedGratitudeCreationDate = gratitude.getCreationDate();
                startActivityForResult(intent, EDIT_GRATITUDE_REQUEST);
            }
        });

        btnCalendar = findViewById(R.id.btnCalendar);
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DayActivity.this, CalendarActivity.class));
            }
        });
        
        //Открываем подключение к БД (или создаем БД, если она не создана, и подключаемся к ней)
        db = new DB(this);
        db.open();

        readGratitudesFromDBInReverseOrder(); //при 1м запуске (когда БД еще пустая) ничего не вернется

        if (gratitudes.isEmpty()) { //из БД ничего не прочли, она пустая - 1й запуск
            for (Gratitude gratitude : createGratitudeList()) //заполняем БД сторонними данными, а не из листа gratitudes
                db.addGratitudeRecord(gratitude.getText(), gratitude.getCreationDate());
            readGratitudesFromDBInReverseOrder(); //читаем данные из только что заполенной БД в лист gratitudes
        }
    }

    private List<Gratitude> createGratitudeList() {
        List<Gratitude> localList = new ArrayList<>();
        long creationDate = new Date().getTime();
        localList.add(new Gratitude(1, "Благодарю за...", creationDate));
        localList.add(new Gratitude(2, "Благодарю за...", creationDate));
        localList.add(new Gratitude(3,"Благодарю за небо над головой и солнышко", creationDate));
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
        Snackbar.make(clGratitudesContainer, R.string.snackbar_text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoDelete();
                        //останавливаем таймер, удаления записи из БД не будет, TimerTask не выполнится
                        timer.cancel(); //Terminates this timer, discarding any currently scheduled tasks
                        timer.purge();  //Removes all cancelled tasks from this timer's task queue. сбросит таймер до конца
                    }
                })
        .show();
        startTimer(); //запускаем таймер с началом показа снекбара
    }

    private void undoDelete() {
        gratitudes.add(recentlyDeletedGratitudePosition, recentlyDeletedGratitude);
        gratitudeAdapter.notifyItemInserted(recentlyDeletedGratitudePosition);
    }

    void startTimer() {
        timer = new Timer();
        //инициализируем работу TimerTask
        initializeTimerTask();
        //Удаление записи из БД по истечении времени показа снекбара (когда пользователь точно не отменил удаление)
        //schedule the timer: after the first 2750ms the TimerTask will run once
        timer.schedule(timerTask, SNACKBAR_LENGTH_LONG);
    }

    void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                db.deleteGratitudeRecord(recentlyDeletedGratitude.getId());
            }
        };
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}