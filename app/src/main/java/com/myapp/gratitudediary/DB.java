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
    public static final String COLUMN_DATE_TIME_OF_RECORD_CREATION = "date_time_of_record_creation";
    public static final String COLUMN_DATE_TIME_OF_RECORD_LAST_EDITION = "date_time_of_record_last_edition";

    //integer из sqlite имеет диапазон long
    private static final String DB_CREATE =
            "create table " + GRATITUDE_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_TEXT + " text, " +
                    COLUMN_DATE_TIME_OF_RECORD_CREATION + " integer, " +
                    COLUMN_DATE_TIME_OF_RECORD_LAST_EDITION + " integer" + //в ms. В SQLite integer включает long. Подбирает размер автоматически
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
    public Cursor getAllRecords() {
        return db.query(GRATITUDE_TABLE, null, null, null, null, null, null);
    }

    //Получить все записи в прямом порядке из GRATITUDE_TABLE в виде List<Gratitude>
    //(делаем две разные функции для получения записей в прямом порядке и в обратном порядке,
    //так как лучше требовать от функций одного поведения - принцип единой ответственности)
    public List<Gratitude> getAllGratitudes() {
        List<Gratitude> gratitudes = new ArrayList<>();
        Cursor cursor = getAllRecords();
        if (cursor.moveToFirst()) { //перемещаемся на 1й эл-т. if обязателен - у нас может не быть данных
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            int textColumnIndex = cursor.getColumnIndex(COLUMN_TEXT);
            int dateTimeOfRecordCreationColumnIndex = cursor.getColumnIndex(COLUMN_DATE_TIME_OF_RECORD_CREATION);
            int dateTimeOfRecordLastEditionColumnIndex = cursor.getColumnIndex(COLUMN_DATE_TIME_OF_RECORD_LAST_EDITION);
            do {
                long gratitudeId = cursor.getLong(idColumnIndex);
                String gratitudeText = cursor.getString(textColumnIndex);
                long dateTimeOfGratitudeCreation = cursor.getLong(dateTimeOfRecordCreationColumnIndex);
                Gratitude gratitude = new Gratitude(gratitudeId, gratitudeText, dateTimeOfGratitudeCreation);
                long editionDate = cursor.getLong(dateTimeOfRecordLastEditionColumnIndex); //если в бд лежит null, то cursor.getLong вернет 0
                if (!cursor.isNull(dateTimeOfRecordLastEditionColumnIndex)) //дата редактирования в бд может быть null, если запись еще не редактировалась
                    gratitude.setEditionDate(editionDate);
                gratitudes.add(gratitude);
                Log.d(LOG_TAG,
                        "ID = " + gratitudeId
                                + ", text = " + gratitudeText
                                + ", date_time_of_record_creation = " + dateTimeOfGratitudeCreation
                                + ", date_time_of_record_last_edition = " + editionDate);
            } while (cursor.moveToNext()); //если есть еще записи
        }
        cursor.close();
        return gratitudes;
    }

    //Получить все записи в обратном порядке из GRATITUDE_TABLE в виде List<Gratitude>
    public List<Gratitude> getAllGratitudesInReverseOrder() {
        List<Gratitude> gratitudes = new ArrayList<>();
        Cursor cursor = getAllRecords();
        if (cursor.moveToLast()) { //перемещаемся на последний эл-т
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            int textColumnIndex = cursor.getColumnIndex(COLUMN_TEXT);
            int dateTimeOfRecordCreationColumnIndex = cursor.getColumnIndex(COLUMN_DATE_TIME_OF_RECORD_CREATION);
            int dateTimeOfRecordLastEditionColumnIndex = cursor.getColumnIndex(COLUMN_DATE_TIME_OF_RECORD_LAST_EDITION);
            do {
                long gratitudeId = cursor.getLong(idColumnIndex);
                String gratitudeText = cursor.getString(textColumnIndex);
                long dateTimeOfGratitudeCreation = cursor.getLong(dateTimeOfRecordCreationColumnIndex);
                Gratitude gratitude = new Gratitude(gratitudeId, gratitudeText, dateTimeOfGratitudeCreation);
                long editionDate = cursor.getLong(dateTimeOfRecordLastEditionColumnIndex);
                if (!cursor.isNull(dateTimeOfRecordLastEditionColumnIndex))
                    gratitude.setEditionDate(editionDate);
                gratitudes.add(gratitude);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        return gratitudes;
    }

    //Добавить запись в GRATITUDE_TABLE
    public long addRecord(String text, long creationDate) { //не Date, так как в БД хранится в integer, который включает long
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT, text);
        cv.put(COLUMN_DATE_TIME_OF_RECORD_CREATION, creationDate);
        long recordId = db.insert(GRATITUDE_TABLE, null, cv);
        return recordId;
    }

    //Добавить запись с заданным id в GRATITUDE_TABLE (используется для восстановления ранее удаленной из бд записи)
    public long addRecord(String text, long creationDate, long id) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT, text);
        cv.put(COLUMN_DATE_TIME_OF_RECORD_CREATION, creationDate);
        cv.put(COLUMN_ID, id);
        long recordId = db.insert(GRATITUDE_TABLE, null, cv);
        return recordId;
    }

    //Обновить запись в GRATITUDE_TABLE по id
    public void updateRecord(long id, String newText, long editionDate) { //дата создания не меняется
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TEXT, newText);
        cv.put(COLUMN_DATE_TIME_OF_RECORD_LAST_EDITION, editionDate);
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
    public void deleteRecord(long id) {
        db.delete(GRATITUDE_TABLE, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
    }

    //Удалить все записи из GRATITUDE_TABLE
    public void deleteAllRecords() {
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
