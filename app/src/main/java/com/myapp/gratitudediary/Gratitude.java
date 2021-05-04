package com.myapp.gratitudediary;

//класс для элементов списка RecyclerView
public class Gratitude {

    private final String text;
    private final long id; //для работы с БД

    public Gratitude(long id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public long getId() {
        return id;
    }
}
