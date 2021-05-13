package com.myapp.gratitudediary;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

//Класс коллбэка, который содержит код, который будет выполняться по свайпу пункта списка
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    //Создаем свой интерфейс для обработки удаления,
    //поскольку хотим реализовать обработку удаления извне (в классе MainActivity), исходя из того кода,
    //который там имеется (нам нужна бд, которая создана в MainActivity, чтобы удалить из нее эл-т)
    public interface OnGratitudeDeleteListener {
        void onGratitudeDelete(int position);
    }

    private final OnGratitudeDeleteListener onGratitudeDeleteListener;

    public SwipeToDeleteCallback(OnGratitudeDeleteListener listener) {
        //задаем направления перетаскивания (не нужны) и направления свайпа пунктов
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        //зададим значение лисенера в конструкторе, а не через сеттер
        onGratitudeDeleteListener = listener;
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        return false;
    }

    //Вызывается, когда элемент свайпается
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (onGratitudeDeleteListener != null) {
            int position = viewHolder.getAdapterPosition();
            onGratitudeDeleteListener.onGratitudeDelete(position);
        }
    }
}
