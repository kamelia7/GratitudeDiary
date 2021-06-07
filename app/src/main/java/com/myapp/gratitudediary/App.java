package com.myapp.gratitudediary;

import android.app.Application;
import android.content.Context;

//Класс App хранит дату, которая была выбрана с помощью DatePickerDialog или стрелок влево-вправо
//на DayActivity или которая была выбрана через CalendarActivity.
//Если мы не выбирали дату с помощью диалога/стрелок на DayActivity или через CalendarActivity
//(например, 1й раз запустили приложение или запустили его после закрытия),
//то в поле даты этого класса будет 0 (дефолтное значение).
//Если в поле даты этого класса 0, то будем выводить текущую дату на DayActivity

public class App extends Application {

    /**
     * приводим к типу App, потому что гарантируем, что то, к чему мы кастуем, точно есть
     * @param context любой контекст, доступный в настоящий момент
     * @return App экземпляр текущего приложения, App прописан в манифесте
     */
    public static App getInstance(Context context) {
        return (App)context.getApplicationContext();
    }

    private long chosenDate = 0; //дефолтное значение

    public long getChosenDate() {
        return chosenDate;
    }

    public void setChosenDate(long chosenDate) {
        this.chosenDate = chosenDate;
    }
}
