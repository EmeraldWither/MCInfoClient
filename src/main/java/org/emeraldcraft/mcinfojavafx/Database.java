package org.emeraldcraft.mcinfojavafx;

import java.sql.*;

public class Database {
    private final String url;
    private final Integer port;
    private final String username;
    private final String name;
    private final String password;
    private Connection connection;
    private ServerInfo serverInfo;
    private Long lastDatabaseConnection;

    public Database(String url, Integer port, String name , String username, String password){
        this.url = url;
        this.name = name;
        this.port = port;
        this.username = username;
        this.password = password;
    }
    public ServerInfo getCachedServerInfo(){
        return this.serverInfo;
    }
    public void setCachedServerInfo(ServerInfo serverInfo){
        this.serverInfo = serverInfo;
    }
    public Long lastDatabaseConnection(){
        return this.lastDatabaseConnection;
    }
    public void testConnection() throws SQLException {
        try {
            this.openConnection();
            if(getConnection() != null && !getConnection().isClosed()){
                System.out.println("Test database connection successful! You are good to go!");
                return;
            }
            System.out.println("There was a problem while opening up the database connection. ");
            Main.shutdown();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("There was a problem while opening up the database connection. ");
            Main.shutdown();
        }
    }
    public void openConnection(){
        try {
            String url = "jdbc:mysql://" + this.url + ":" + this.port + "/" + this.name;
            // try catch to get any SQL errors (for example connections errors)
            connection = DriverManager.getConnection(url, username, password);

            // with the method getConnection() from DriverManager, we're trying to set
            // the connection's url, username, password to the variables we made earlier and
            // trying to get a connection at the same time. JDBC allows us to do this.
        } catch (SQLException e) { // catching errors
            e.printStackTrace(); // prints out SQLException errors to the console (if any)
        }
    }
    public void closeConnection(){
        try {
            if(getConnection() != null && !getConnection().isClosed()){
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Getter and Setters
    public Connection getConnection(){
        return this.connection;
    }

    public ServerInfo getServerInfo(){
        boolean isOnline = false;
        int onlinePlayers = 0;
        int maxPlayers = 10;
        int tps = 20;
        //Default mcVersion to latest.
        String mcVersion = "latest";
        String serverName = "A Minecraft Server";
        //Create our vars to return

        try {
            this.lastDatabaseConnection = System.currentTimeMillis();
            if(getConnection() == null || getConnection().isClosed()){
                openConnection();
            }
            Connection connection = getConnection();
            String sqlcreateTable = "create table if not exists serverinfo(onlinePlayers integer(7), maxPlayers integer(10), isOnline boolean, mcVersion varchar(7), tps integer(3), serverName varchar(1000));";
            String sqlSelect = "SELECT * from serverinfo;";

            // Create table
            PreparedStatement stmt = connection.prepareStatement(sqlcreateTable);
            stmt.executeUpdate();


            PreparedStatement stmt2 = connection.prepareStatement(sqlSelect);
            ResultSet results = stmt2.executeQuery();
            while (results.next()) {
                isOnline = results.getBoolean("isOnline");
                onlinePlayers = results.getInt("onlinePlayers");
                maxPlayers = results.getInt("maxPlayers");
                tps = results.getInt("tps");
                mcVersion = results.getString("mcVersion");
                serverName = results.getString("serverName");
                System.out.println("Servername" + serverName);
            }

        }
        catch (SQLException e){
            System.out.println("A database error has occurred!");
            e.printStackTrace();
        }
        closeConnection();
        this.serverInfo = new ServerInfo(isOnline, onlinePlayers, maxPlayers, tps, mcVersion, serverName);
        System.out.println(serverInfo);
        return new ServerInfo(isOnline, onlinePlayers, maxPlayers, tps, mcVersion, serverName);
    }

}
