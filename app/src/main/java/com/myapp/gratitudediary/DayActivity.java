package com.myapp.gratitudediary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DayActivity extends AppCompatActivity {

    RecyclerView rvGratitudes;
    List<Gratitude> gratitudes = new ArrayList<>();
    final GratitudeAdapter gratitudeAdapter = new GratitudeAdapter(gratitudes);
    FloatingActionButton fabAddRecord;
    CoordinatorLayout clGratitudesContainer;
    Button btnCalendar;
    TextView tvDate;

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

    SimpleDateFormat sdfDayAndMonth = new SimpleDateFormat("E, dd MMMM", Locale.getDefault()); //dd MMMM yyyy

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
                    long creationDate = System.currentTimeMillis();
                    long recordId = db.addGratitudeRecord(recordText, creationDate);
                    gratitudes.add(0, new Gratitude(recordId, recordText, creationDate));
                    gratitudeAdapter.notifyItemInserted(0); //так появляется анимация
                    break;

                case EDIT_GRATITUDE_REQUEST:
                    long editionDate = System.currentTimeMillis();
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
                //Открываем CalendarActivity из DayActivity, НЕ уничтожаем DayActivity (нет finish()),
                //чтобы если не выберем карточку с датой на CalendarActivity и нажмем Back,
                //то мы попадали на DayActivity с последней выбранной датой
                //(если дату не выбирали, то откроется на текущей дате)
            }
        });

        final App app = App.getInstance(this);

        tvDate = findViewById(R.id.tvDate);
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startDate = app.getChosenDate();
                final Calendar c = Calendar.getInstance(); //если не устанавливали дату сами ранее, то откроем диалог на текущей дате
                if (startDate != 0) {
                    c.clear();
                    c.setTimeInMillis(startDate);
                }

                DatePickerDialog dpd = new DatePickerDialog(DayActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        c.clear();
                        c.set(year, month, dayOfMonth);
                        tvDate.setText(sdfDayAndMonth.format(c.getTime()));
                        long chosenDateMillis = c.getTimeInMillis();
                        app.setChosenDate(chosenDateMillis); //сохраняем выбранную с помощью диалога дату

                        readGratitudesForGivenDayAndMonthAndYearFromDBInReverseOrder(chosenDateMillis); //читаем благодарности за выбранный день
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                dpd.show();
            }
        });

        //Открываем подключение к БД (или создаем БД, если она не создана, и подключаемся к ней)
        db = new DB(this);
        db.open();

        //Здесь в onCreate (до выбора даты пользоваталем)
        //нельзя просто всегда выводить текущую дату и записи за нее,
        //так как при нажатии Back на DayActivity эта активити уничтожится,
        //и приложение будет просто лежать в памяти (оно не закроется, не уйдет в фон,
        //а Application будет продолжать существовать нек. время, потом система его уничтожит),
        //а при восстановлении приложения из памяти и, соответственно, вызове onCreate для DayActivity, нужно,
        //чтобы на DayActivity была открыта последняя установленная дата и записи за нее, а не текущая дата
        displayDateFromAppOrCurrentDateAndGratitudesForDate();

        /*
        readGratitudesFromDBInReverseOrder(); //при 1м запуске (когда БД еще пустая) ничего не вернется

        if (gratitudes.isEmpty()) { //из БД ничего не прочли, она пустая - 1й запуск
            for (Gratitude gratitude : createGratitudeList()) //заполняем БД сторонними данными, а не из листа gratitudes
                db.addGratitudeRecord(gratitude.getText(), gratitude.getCreationDate());
            readGratitudesFromDBInReverseOrder(); //читаем данные из только что заполенной БД в лист gratitudes
        }
        */
    }

    //Вывод даты в tvDate: выводим ранее выбранную пользоваталем дату (лежит в поле класса App),
    //иначе - текущую
    //(дата может быть выбрана через диалог/стрелки на DayActivity или через CalendarActivity;
    //выбор через CalendarActivity учитывается, т.к. могли выбрать карточку с датой на CalendarActivity
    //(в этот момент выбранная дата сохранилась в App), перейти на DayActivity и нажать Back).
    //Вывод записей за выбранную пользователем дату;
    //если дата не была ранее выбрана, то выводим записи за текущий день
    private void displayDateFromAppOrCurrentDateAndGratitudesForDate() {
        App app = App.getInstance(this);
        long chosenDateMillis = app.getChosenDate();
        if (chosenDateMillis != 0) {
            tvDate.setText(sdfDayAndMonth.format(chosenDateMillis));
            readGratitudesForGivenDayAndMonthAndYearFromDBInReverseOrder(chosenDateMillis);
        }
        else {
            long currentTimeMillis = System.currentTimeMillis();
            tvDate.setText(sdfDayAndMonth.format(currentTimeMillis));
            readGratitudesForGivenDayAndMonthAndYearFromDBInReverseOrder(currentTimeMillis);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        displayDateFromAppOrCurrentDateAndGratitudesForDate();
    }

    private List<Gratitude> createGratitudeList() {
        List<Gratitude> localList = new ArrayList<>();
        long creationDate = System.currentTimeMillis();
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

    //выводит благодарности за указанную дату (за указанный день, месяц и год)
    private void readGratitudesForGivenDayAndMonthAndYearFromDBInReverseOrder(long dateWithGivenDayAndMonthAndYear) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateWithGivenDayAndMonthAndYear);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        c.clear();

        //нижняя граница - указанный день указанного месяца указанного года - включаем
        c.set(year, month, dayOfMonth, 0, 0, 0);
        long includedLowerLimit = c.getTimeInMillis();
        //верхняя граница - следующий день указанного месяца указанного года - не включаем
        c.add(Calendar.DAY_OF_MONTH, 1);
        long excludedUpperLimit = c.getTimeInMillis();

        gratitudes.clear();
        List<Gratitude> list = new ArrayList<>();
        list.addAll(db.getGratitudesForTimePeriod(includedLowerLimit, excludedUpperLimit));
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
