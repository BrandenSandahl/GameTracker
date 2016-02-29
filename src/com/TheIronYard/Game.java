package com.TheIronYard;

/**
 * Created by branden on 2/23/16 at 11:13.
 */
public class Game {

    String name, genre, platform, owner;
    int releaseYear, id;

    public Game(String name, String genre, String platform, int releaseYear, String owner) {
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
        this.owner = owner;
    }

    public Game() {
    }
}