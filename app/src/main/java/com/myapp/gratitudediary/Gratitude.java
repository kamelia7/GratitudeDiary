package com.myapp.gratitudediary;

//класс для элементов списка RecyclerView
public class Gratitude {
    //позже сюда будет добавлено ещё id для БДшки
    private final String text;

    public Gratitude(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
