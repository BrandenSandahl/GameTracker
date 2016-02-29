package com.TheIronYard;

import java.util.ArrayList;

/**
 * Created by branden on 2/23/16 at 11:11.
 */
public class User {

    String name;
    Boolean hasGames = false;
    ArrayList<Game> games = new ArrayList<>();


    public User(String name) {
        this.name = name;
    }


    public Boolean getHasGames() {
        return hasGames;
    }

    public void setHasGames(Boolean hasGames) {
        this.hasGames = hasGames;
    }
}

