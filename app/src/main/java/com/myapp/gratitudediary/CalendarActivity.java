package com.myapp.gratitudediary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

//CalendarActivity отображает карточки в RecyclerView с днями за данный месяц и год,
//в которые были добавлены какие-либо благодарности.
//Для этого из БД считываются даты создания записей (в мс) за данный месяц и год,
//затем среди них отбираются только даты с уникальными днями месяца,
//а на активити отображаются дни этих отобранных дат.

public class CalendarActivity extends AppCompatActivity {

    RecyclerView rvDates;
    List<Long> datesForGivenMonthAndYearWithUniqueDayOfMonth = new ArrayList();
    final DateAdapter dateAdapter = new DateAdapter(datesForGivenMonthAndYearWithUniqueDayOfMonth);
    DB db;

    TextView tvYear;
    TextView tvMonth;

    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.getDefault());
    SimpleDateFormat sdfMonth = new SimpleDateFormat("LLLL", Locale.getDefault()); //stand-alone month

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        rvDates = findViewById(R.id.rvDates);

        rvDates.setLayoutManager(new GridLayoutManager(this, 4));
        rvDates.setAdapter(dateAdapter);

        final App app = App.getInstance(this);

        dateAdapter.setOnDateClickListener(new DateAdapter.OnDateClickListener() {
            @Override
            public void onDateClick(long date, int position) {
                app.setChosenDate(date); //сохраняем выбранную дату
                finish();
                //вообще не запускаем DayActivity,
                //таким образом не будет кучи DayActivity, которые по очереди будут открываться
                //при нажатии Back на DayActivity.
                //Сохраняем выбранную дату и закрываем CalendarActivity (она уничтожится).
                //После чего вызовется onRestart (onRestart -> onStart -> onResume)
                //для DayActivity, потому что она не была уничтожена (не вызывалось finish()),
                //а находилась в состоянии Stopped, пока было открыто CalendarActivity.
            }
        });

        tvYear = findViewById(R.id.tvYear);
        tvMonth = findViewById(R.id.tvMonth);

        db = new DB(this);
        db.open();

        //Вывод года и месяца в tvYear и tvMonth: выводим год и месяц ранее выбранной пользоваталем даты
        //(лежит в поле класса App), иначе выводим год и месяц текущей даты
        //(дата может быть выбрана через диалог/стрелки на DayActivity или через CalendarActivity;
        //выбор через CalendarActivity учитывается, так как могли нажать на карточку с датой
        //на CalendarActivity (в этот момент выбранная дата сохранилась в App),
        //перейти на DayActivity и потом нажать на btnCalendar,
        //по клику на которую происходит открытие CalendarActivity).
        //Вывод уникальных дней дат благодарностей за месяц и год,
        //соответствующие выбранной пользователем дате;
        //если дата не была ранее выбрана, то выводим уникальные дни дат благодарностей
        //за год и месяц текущей даты (т.е. за текущий месяц)
        long chosenDateMillis = app.getChosenDate();
        if (chosenDateMillis != 0) {
            tvYear.setText(sdfYear.format(chosenDateMillis));
            tvMonth.setText(sdfMonth.format(chosenDateMillis));
            readGratitudesDatesForGivenMonthAndYearFromDB(chosenDateMillis);
        }
        else {
            long currentTimeMillis = System.currentTimeMillis();
            tvYear.setText(sdfYear.format(currentTimeMillis));
            tvMonth.setText(sdfMonth.format(currentTimeMillis));
            readGratitudesDatesForGivenMonthAndYearFromDB(currentTimeMillis);
        }
    }

    //выводит уникальные дни дат благодарностей, отобранных за данный месяц и год
    private void readGratitudesDatesForGivenMonthAndYearFromDB(long dateWithGivenMonthAndYear) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateWithGivenMonthAndYear);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        c.clear();

        c.set(year, month, 1, 0, 0, 0);
        //нижняя граница - 1й день указанного месяца указанного года - включаем
        long includedLowerLimit = c.getTimeInMillis();
        c.add(Calendar.MONTH, 1);
        //верхняя граница - 1й день следующего месяца указанного года - не включаем
        long excludedUpperLimit = c.getTimeInMillis();

        Set<Long> datesSetWithUniqueDayOfMonth = new HashSet<>();
        for (long recordDate : db.getGratitudesDatesForTimePeriod(includedLowerLimit, excludedUpperLimit)) {
            c.setTimeInMillis(recordDate);
            int recordYear = c.get(Calendar.YEAR);
            int recordMonth = c.get(Calendar.MONTH);
            int recordDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            c.clear();
            //обнуляем все, кроме года, месяца и дня, чтобы даты могли различаться только днем
            //(так как месяц и год у них одинаковый: записи отобраны за конкретный месяц и год)
            c.set(recordYear, recordMonth, recordDayOfMonth, 0, 0, 0);
            datesSetWithUniqueDayOfMonth.add(c.getTimeInMillis());
        }
        datesForGivenMonthAndYearWithUniqueDayOfMonth.clear();
        datesForGivenMonthAndYearWithUniqueDayOfMonth.addAll(datesSetWithUniqueDayOfMonth);
        Collections.sort(datesForGivenMonthAndYearWithUniqueDayOfMonth);
        dateAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
