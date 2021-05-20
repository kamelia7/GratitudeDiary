package com.myapp.gratitudediary;

//класс для элементов списка RecyclerView
public class Gratitude {

    private final String text;
    private long creationDate;
    private final long id; //для работы с БД

    private long editionDate; //по умолчанию 0, если не задать в конструкторе

    public Gratitude(long id, String text, long creationDate) {
        this.id = id;
        this.text = text;
        this.creationDate = creationDate;
        this.editionDate = 0; //запись еще не редактировалась
    }

    //конструктор для редактирования записи
    public Gratitude(long id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public long getId() {
        return id;
    }

    public void setEditionDate(long editionDate) {
        this.editionDate = editionDate;
    }
}
