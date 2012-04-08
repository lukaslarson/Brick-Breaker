package com.luklar9.assignment3;

// this class will handle a high-score database
final class SQLiteHandler {
    private static String name;
    private static int score;

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
