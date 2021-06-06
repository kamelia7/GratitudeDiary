package com.myapp.gratitudediary;

import android.app.Application;
import android.content.Context;

//Класс App хранит дату, которая была выбрана с помощью DatePickerDialog на DayActivity.
//Если мы не выбирали дату с помощью диалога
//(например, 1й раз запустили приложение или запустили его после закрытия),
//то в поле даты этого класса будет null.
//Если в поле даты этого класса null, то будем выводить текущую дату на DayActivity

public class App extends Application {

    /**
     * приводим к типу App, потому что гарантируем, что то, к чему мы кастуем, точно есть
     * @param context любой контекст, доступный в настоящий момент
     * @return App экземпляр текущего приложения, App прописан в манифесте
     */
    public static App getInstance(Context context) {
        return (App)context.getApplicationContext();
    }

    private long dateFromDatePickerDialog;

    public long getDateFromDatePickerDialog() {
        return dateFromDatePickerDialog;
    }

    public void setDateFromDatePickerDialog(long dateFromDatePickerDialog) {
        this.dateFromDatePickerDialog = dateFromDatePickerDialog;
    }
}
