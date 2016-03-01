package com.TheIronYard;

import spark.*;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static boolean userSearch; //this is not very clever way to accomplish this. Anyone have better?

    public static void main(String[] args) throws SQLException {
        Spark.externalStaticFileLocation("public");
        Spark.init();

        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement statement = conn.createStatement();

        //create new tables if does not exist
        statement.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, name VARCHAR, genre VARCHAR, platform VARCHAR, release_year INT, owner VARCHAR)");
        statement.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR)");

        Spark.get(
                "/",
                ((request, response) -> {
                    String loggedInName = getUserFromSession(request.session());

                    HashMap m = new HashMap();
                    ArrayList<Game> games = new ArrayList<>(); //arraylist for displaying info from the DB

                    if  (loggedInName == null) {  //if not logged in yet, lot in
                        return new ModelAndView(m, "login.html");
                    } else if (userSearch) { //if logged in and the user is trying to search
                        String searchQuery = request.queryParams("searchQuery");
                        games = selectGames(conn,loggedInName, searchQuery);
                        userSearch = false; //reset option
                        m.put("games", games);
                        return new ModelAndView(m, "home.html");
                    } else {  //if logged in display the users games
                           games = selectGames(conn, loggedInName); //local list from the DB
                        m.put("games", games);
                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );

        Spark.get(
                "/edit",
                ((request2, response2) ->  {
                    int id = Integer.parseInt(request2.queryParams("id"));
                    Game game = selectSingleGame(conn, id);  //create a copy of the game to work with from the DB
                    return new ModelAndView(game, "edit.html");

                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/search",
                ((request3, response3) -> {
                    String searchQuery = request3.queryParams("searchTitleInput");

                    response3.redirect("/?searchQuery=" + searchQuery ); //just need to pull this in and push it back to the get route
                    userSearch = true;
                    return "";
                })
        );

        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String loggedInName = request.queryParams("loginName");
                    if (loggedInName == null) throw new Exception("Login name is null");

                    Session session = request.session();

                    //need to get my users from DB and see if there is already a user
                    PreparedStatement statement1 = conn.prepareStatement("SELECT name FROM users WHERE name = ?");
                    statement1.setString(1, loggedInName);
                    ResultSet resultSet = statement1.executeQuery();

                    if (resultSet != null) {  //if we found anything in the DB
                            session.attribute("userName", loggedInName);  //log in as that user
                    } else {
                            //put into DB and log in as the new user
                            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?)");
                            preparedStatement.setString(1, loggedInName);
                            preparedStatement.execute();
                            session.attribute("userName", loggedInName);
                    }
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/create-game",
                ((request, response) ->  {
                    //pull up the logged in user
                    String loggedInAs = getUserFromSession(request.session()); //pulling a pointer to the user in the HashMap

                    if (loggedInAs == null) {
                        Spark.halt(403); //throwing a spark error because user is not logged in.
                    }

                    //get field from form
                    String gameName = request.queryParams("gameName");
                    String gameGenre = request.queryParams("gameGenre");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request.queryParams("gameYear"));
                    if (gameName == null || gameGenre == null || gamePlatform == null ) {
                        throw new Exception("didn't receive query params");

                    }

                    //create a game
                    Game game = new Game(gameName, gameGenre, gamePlatform, gameYear, loggedInAs);
                    // add to DB
                    createGame(conn, game);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/edit-game",
                ((request2, response2) -> {
                    int id = Integer.parseInt(request2.queryParams("id"));

                    String gameName = request2.queryParams("gameName");
                    String gameGenre = request2.queryParams("gameGenre");
                    String gamePlatform = request2.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request2.queryParams("gameYear"));

                    PreparedStatement statement1 = conn.prepareStatement("UPDATE games SET name = ?, genre = ?, platform = ?, release_year = ? WHERE id = ?");
                    statement1.setString(1, gameName);
                    statement1.setString(2, gameGenre);
                    statement1.setString(3, gamePlatform);
                    statement1.setInt(4, gameYear);
                    statement1.setInt(5, id);
                    statement1.execute();

                    response2.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete",
                ((request1, response1) -> {
                    int id = Integer.parseInt(request1.queryParams("id"));

                   //prepared statment here
                    PreparedStatement statement1 = conn.prepareStatement("DELETE FROM games WHERE id = ?");
                    statement1.setInt(1, id);
                    statement1.execute();

                    response1.redirect("/");
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

    static String getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return  name;
    }

    static void createGame(Connection conn, Game g) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO games VALUES (NULL, ?, ?, ?, ?, ?)");
        preparedStatement.setString(1, g.name);
        preparedStatement.setString(2, g.genre);
        preparedStatement.setString(3, g.platform);
        preparedStatement.setInt(4, g.releaseYear);
        preparedStatement.setString(5, g.owner);
        preparedStatement.execute();

    }


    static ArrayList<Game> selectGames(Connection conn, String loggedInName) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();

        PreparedStatement statement1 = conn.prepareStatement("SELECT * FROM games WHERE owner = ?");
            statement1.setString(1, loggedInName);

            ResultSet resultSet = statement1.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String genre = resultSet.getString("genre");
                String platform = resultSet.getString("platform");
                int releaseYear = resultSet.getInt("release_year");
                String owner = resultSet.getString("owner");

                Game game = new Game(name, genre, platform, releaseYear, owner); //create a game
                game.id = id;  // set the id from the DB to the id of the game object
                games.add(game); //add to temp list for display and interaction purposes.
            }

        return games;
    }

    //OOOOVVVEEERRLLOOOOAAADDDD. yeah..it's an overloaded method.
    static ArrayList<Game> selectGames(Connection conn, String loggedInName, String searchQuery) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        PreparedStatement statement1 = conn.prepareStatement("SELECT * FROM games WHERE owner = ? AND LOWER(name) LIKE LOWER(?) ");
        statement1.setString(1, loggedInName);
   //   SEE: http://stackoverflow.com/questions/8247970/using-like-wildcard-in-prepared-statement
        statement1.setString(2, "%" + searchQuery + "%");  //these %'s can not be in the prepare statement part for whatever reason

        ResultSet resultSet = statement1.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String genre = resultSet.getString("genre");
            String platform = resultSet.getString("platform");
            int releaseYear = resultSet.getInt("release_year");
            String owner = resultSet.getString("owner");

            Game game = new Game(name, genre, platform, releaseYear, owner); //create a game
            game.id = id;  // set the id from the DB to the id of the game object
            games.add(game); //add to temp list for display and interaction purposes.
        }

        return games;


    }


    static Game selectSingleGame(Connection conn, int idIndex) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM games WHERE id = ?");
        statement.setInt(1, idIndex);

        ResultSet resultSet = statement.executeQuery();
        Game game = new Game();

        if (resultSet.next()) {
            String name = resultSet.getString("name");
            String genre = resultSet.getString("genre");
            String platform = resultSet.getString("platform");
            int releaseYear = resultSet.getInt("release_year");
            String owner = resultSet.getString("owner");

            game = new Game(name, genre, platform, releaseYear, owner); //create a game
            game.id = idIndex;
        }
        return game;
    }

}
