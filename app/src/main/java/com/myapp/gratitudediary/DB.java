package com.myapp.gratitudediary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DB {
    private static final String DB_NAME = "gratitude_db";
    private static final int DB_VERSION = 1;
    private static String GRATITUDE_TABLE = "gratitude_table";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_DATE_MILLIS_OF_RECORD_CREATION = "date_millis_of_record_creation";
    public static final String COLUMN_DATE_MILLIS_OF_RECORD_LAST_EDITION = "date_millis_of_record_last_edition";

    //integer из sqlite имеет диапазон long
    private static final String DB_CREATE =
            "create table " + GRATITUDE_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_TEXT + " text, " +
                    COLUMN_DATE_MILLIS_OF_RECORD_CREATION + " integer, " +
                    COLUMN_DATE_MILLIS_OF_RECORD_LAST_EDITION + " integer" + //в ms. В SQLite integer включает long. Подбирает размер автоматически
                    ");";

    final String LOG_TAG = "myLogs";

    private final Context ctx;

    private DBHelper dbh;
    private SQLiteDatabase db;

    public DB(Context ctx) {
        this.ctx = ctx;
    }

    //Открыть подключение
    public void open() {
        dbh = new DBHelper(ctx, DB_NAME, null, DB_VERSION);
        db = dbh.getWritableDatabase(); //вызовет onCreate, если БД не создана, иначе вернет существующую БД
    }

    //Закрыть подключение
    public void close() {
        if (dbh != null) dbh.close();
    }

    //Получить все записи из GRATITUDE_TABLE в виде Cursor
    public Cursor getAllGratitudesRecords() {
        return db.query(GRATITUDE_TABLE, null, null, null, null, null, null);
    }

    //Получить даты создания записей (в мс) за указанный период времени из GRATITUDE_TABLE в виде Cursor
    //нижняя граница включается, верхняя - не включается
    public Cursor getGratitudesRecordsDatesForTimePeriod(long includedLowerLimit, long excludedUpperLimit) {

        String sqlQuery = "SELECT " + COLUMN_DATE_MILLIS_OF_RECORD_CREATION
                + " FROM " + GRATITUDE_TABLE
                + " WHERE " + COLUMN_DATE_MILLIS_OF_RECORD_CREATION + " >= " + includedLowerLimit
                + " AND " + COLUMN_DATE_MILLIS_OF_RECORD_CREATION + " < " + excludedUpperLimit;

        return db.rawQuery(sqlQuery, null);
    }

    //Получить все записи в прямом порядке из GRATITUDE_TABLE в виде List<Gratitude>
    //(делаем две разные функции для получения записей в прямом порядке и в обратном порядке,
    //так как лучше требовать от функций одного поведения - принцип единой ответственности)
    public List<Gratitude> getAllGratitudes() {
        List<Gratitude> gratitudes = new ArrayList<>();
        Cursor cursor = getAllGratitudesRecords();
        if (cursor.moveToFirst()) { //перемещаемся на 1й эл-т. if обязателен - у нас может не быть данных
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            int textColumnIndex = cursor.getColumnIndex(COLUMN_TEXT);
            int dateMillisOfRecordCreationColumnIndex = cursor.getColumnIndex(COLUMN_DATE_MILLIS_OF_RECORD_CREATION);
            int dateMillisOfRecordLastEditionColumnIndex = cursor.getColumnIndex(COLUMN_DATE_MILLIS_OF_RECORD_LAST_EDITION);
            do {
                long gratitudeId = cursor.getLong(idColumnIndex);
                String gratitudeText = cursor.getString(textColumnIndex);
                long dateMillisOfGratitudeCreation = cursor.getLong(dateMillisOfRecordCreationColumnIndex);
                Gratitude gratitude = new Gratitude(gratitudeId, gratitudeText, dateMillisOfGratitudeCreation);
                long editionDate = cursor.getLong(dateMillisOfRecordLastEditionColumnIndex); //если в бд лежит null, то cursor.getLong вернет 0
                if (!cursor.isNull(dateMillisOfRecordLastEditionColumnIndex)) //дата редактирования в бд может быть null, если запись еще не редактировалась
                    gratitude.setEditionDate(editionDate);
                gratitudes.add(gratitude);
                Log.d(LOG_TAG,
                        "ID = " + gratitudeId
                                + ", text = " + gratitudeText
                                + ", date_millis_of_record_creation = " + dateMillisOfGratitudeCreation
                                + ", date_millis_of_record_last_edition = " + editionDate);
            } while (cursor.moveToNext()); //если есть еще записи
        }
        cursor.close();
        return gratitudes;
    }

    //Получить все записи в обратном порядке из GRATITUDE_TABLE в виде List<Gratitude>
    public List<Gratitude> getAllGratitudesInReverseOrder() {
        List<Gratitude> gratitudes = new ArrayList<>();
        Cursor cursor = getAllGratitudesRecords();
        if (cursor.moveToLast()) { //перемещаемся на последний эл-т
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            int textColumnIndex = cursor.getColumnIndex(COLUMN_TEXT);
            int dateMillisOfRecordCreationColumnIndex = cursor.getColumnIndex(COLUMN_DATE_MILLIS_OF_RECORD_CREATION);
            int dateMillisOfRecordLastEditionColumnIndex = cursor.getColumnIndex(COLUMN_DATE_MILLIS_OF_RECORD_LAST_EDITION);
            do {
                long gratitudeId = cursor.getLong(idColumnIndex);
                String gratitudeText = cursor.getString(textColumnIndex);
                long dateMillisOfGratitudeCreation = cursor.getLong(dateMillisOfRecordCreationColumnIndex);
                Gratitude gratitude = new Gratitude(gratitudeId, gratitudeText, dateMillisOfGratitudeCreation);
                long editionDate = cursor.getLong(dateMillisOfRecordLastEditionColumnIndex);
                if (!cursor.isNull(dateMillisOfRecordLastEditionColumnIndex))
                    gratitude.setEditionDate(editionDate);
                gratitudes.add(gratitude);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        return gratitudes;
    }

    //Получить даты создания записей (в мс) за указанный период времени из GRATITUDE_TABLE в виде List<Long>
    //нижняя граница включается, верхняя - не включается
    public List<Long> getGratitudesDatesForTimePeriod(long includedLowerLimit, long excludedUpperLimit) {
        List<Long> dates = new ArrayList<>();
        Cursor cursor = getGratitudesRecordsDatesForTimePeriod(includedLowerLimit, excludedUpperLimit);
        Log.d(LOG_TAG, "getGratitudesDatesForTimePeriod: includedLowerLimit = "
                + includedLowerLimit + " excludedUpperLimit = " + excludedUpperLimit);
        int i = 0;
        if (cursor.moveToFirst()) {
            int dateMillisOfRecordCreationColumnIndex = cursor.getColumnIndex(COLUMN_DATE_MILLIS_OF_RECORD_CREATION);
            do {
                long date = cursor.getLong(dateMillisOfRecordCreationColumnIndex);
                dates.add(date);
                i++;
                Log.d(LOG_TAG, "date_millis_of_record_creation = " + date);
                Log.d(LOG_TAG, "i = " + i); //быстрофикс для выведения всех записей в лог. иначе 1я запись в лог почему-то не попадает, хотя на экран выводится
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dates;
    }

    //Добавить запись в GRATITUDE_TABLE
    public long addGratitudeRecord(String text, long creationDate) { //не Date, так как в БД хранится в integer, который включает long
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT, text);
        cv.put(COLUMN_DATE_MILLIS_OF_RECORD_CREATION, creationDate);
        long recordId = db.insert(GRATITUDE_TABLE, null, cv);
        return recordId;
    }

    //Добавить запись с заданным id в GRATITUDE_TABLE (используется для восстановления ранее удаленной из бд записи)
    public long addGratitudeRecord(String text, long creationDate, long id) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT, text);
        cv.put(COLUMN_DATE_MILLIS_OF_RECORD_CREATION, creationDate);
        cv.put(COLUMN_ID, id);
        long recordId = db.insert(GRATITUDE_TABLE, null, cv);
        return recordId;
    }

    //Обновить запись в GRATITUDE_TABLE по id
    public void updateGratitudeRecord(long id, String newText, long editionDate) { //дата создания не меняется
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT, newText);
        cv.put(COLUMN_DATE_MILLIS_OF_RECORD_LAST_EDITION, editionDate);
        db.update(GRATITUDE_TABLE, cv, COLUMN_ID + " = " + id, null); //update возвращает int updRecordsCount
        //обновлять можем по какому-либо полю таблицы
        //то есть, либо по id,
        //либо по тексту (не подходит, т.к. если текст неск записей случайно совпал, то все эти записи обновятся)
        //(аналогично для удаления записи)
        //отправлять зн-е position в кач-ве id не получится, т.к. это зн-е не совпадает с id
        //(напр., могли удалять текст из бд и списка, id в бд при этом не сдвигается, а position в списке сдвинется)
        //=> нужен уникальный идентификатор записи для обновления и удаления
    }

    //Удалить запись из GRATITUDE_TABLE по id
    public void deleteGratitudeRecord(long id) {
        db.delete(GRATITUDE_TABLE, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
    }

    //Удалить все записи из GRATITUDE_TABLE
    public void deleteAllGratitudesRecords() {
        db.delete(GRATITUDE_TABLE, null, null); //delete возвращает int delRecordsCount
    }

    //Класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //Создание БД (заполнение не здесь)
        //вызывается после dbh.getWritableDatabase(), если БД не создана
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
