package com.myapp.gratitudediary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        rvDates = findViewById(R.id.rvDates);

        rvDates.setLayoutManager(new GridLayoutManager(this, 4));
        rvDates.setAdapter(dateAdapter);

        dateAdapter.setOnDateClickListener(new DateAdapter.OnDateClickListener() {
            @Override
            public void onDateClick(long date, int position) {
                startActivityForResult(new Intent(CalendarActivity.this, DayActivity.class), 0);
            }
        });

        db = new DB(this);
        db.open();

        readGratitudesDatesForGivenMonthAndYearFromDB(System.currentTimeMillis()); //TODO сюда будет приходить дата из пикера в мс
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
