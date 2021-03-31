package com.myapp.gratitudediary;

//класс для элементов списка RecyclerView
public class Gratitude {
    private final String text;

    public Gratitude(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
