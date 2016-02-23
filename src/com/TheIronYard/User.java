package com.TheIronYard;

import java.util.ArrayList;

/**
 * Created by branden on 2/23/16 at 11:11.
 */
public class User {

    String name;
    ArrayList<Game> games = new ArrayList<>();


    public User(String name) {
        this.name = name;
    }
}