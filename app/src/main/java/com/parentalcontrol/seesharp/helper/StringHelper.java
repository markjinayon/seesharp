package com.parentalcontrol.seesharp.helper;

import java.util.Random;

public class StringHelper {
    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String generatePin() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}


