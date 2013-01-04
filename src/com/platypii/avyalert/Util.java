package com.platypii.avyalert;


public class Util {

    /** Equality checker that doesn't NullPointerException */
    public static boolean eq(Object a, Object b) {
        return a == null? b == null : a.equals(b);
    }

}
