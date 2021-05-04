package com.myapp.gratitudediary;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static <T> List<T> getReverseList(List<T> list) {
        List<T> reverseList = new ArrayList<>();
        for (T entry : list)
            reverseList.add(0, entry);
        return reverseList;
    }
}
