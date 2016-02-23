package com.TheIronYard;

/**
 * Created by branden on 2/23/16 at 11:13.
 */
public class Game {

    String name, genre, platform;
    int releaseYear;

    public Game(String name, String genre, String platform, int releaseYear) {
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}