package com.TheIronYard;

import spark.*;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;

public class Main {

   static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) {
        Spark.init();


        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    HashMap m = new HashMap();
                    if  (user == null) {
                        return new ModelAndView(m, "login.html"); //can make a hashmap
                    } else {
                        return new ModelAndView(user, "home.html"); //or can pass a user
                    }
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }

                    Session session = request.session();
                    session.attribute("userName", name);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/create-game",
                ((request, response) ->  {
                    //pull up the logged in user
                    User user = getUserFromSession(request.session()); //pulling a pointer to the user in the HashMap

                    if (user == null) {
                        //throw new Exception("User is not logged in");
                        Spark.halt(403); //throwing a spark error because user is not logged in.
                    }

                    String gameName = request.queryParams("gameName");
                    String gameGenre = request.queryParams("gameGenre");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request.queryParams("gameYear"));

                    //create a game
                    Game game = new Game(gameName, gameGenre, gamePlatform, gameYear);

                    //add game to the loggin in user
                    user.games.add(game);
                  //  users.get(user.name).games.add(game); //don't need to do this.
                    response.redirect("/");


                    return "";
                })
        );

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

    }

    static User getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return  users.get(name);

    }

}
