package com.luklar9.assignment3;

/**
 * Created with IntelliJ IDEA.
 * User: Chokis
 * Date: 2012-04-05
 * Time: 17:22
 * To change this template use File | Settings | File Templates.
 */
public final class SQLiteHandler {
    public static String name;
    public static int score;

    public static int getScore() {
        return score;
    }

    public static void setScore(int score) {
        SQLiteHandler.score = score;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        SQLiteHandler.name = name;
    }
}
