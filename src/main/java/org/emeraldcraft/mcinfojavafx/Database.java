package org.emeraldcraft.mcinfojavafx;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

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

    public void openConnection(){
        try {
            String url = "jdbc:mysql://" + this.url + ":" + this.port + "/" + this.name;
            connection = DriverManager.getConnection(url, username, password);
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
            if (getConnection() == null || getConnection().isClosed()) {
                openConnection();
            }
            Connection connection = getConnection();
            String sqlcreateTable = "CREATE TABLE IF NOT EXISTS serverinfo(onlinePlayers integer(7), maxPlayers integer(10), isOnline boolean, mcVersion varchar(7), tps integer(3), serverName varchar(1000));";
            String sqlSelect = "SELECT * FROM serverinfo;";

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
            }
            closeConnection();
        }
        catch (SQLException e){
            System.out.println("A database error has occurred!");
            closeConnection();
            e.printStackTrace();
        }
        closeConnection();
        this.serverInfo = new ServerInfo(isOnline, onlinePlayers, maxPlayers, tps, mcVersion, serverName);
        return new ServerInfo(isOnline, onlinePlayers, maxPlayers, tps, mcVersion, serverName);
    }

    public void queueCommand(String command){
        try {
            this.lastDatabaseConnection = System.currentTimeMillis();
            if (getConnection() == null || getConnection().isClosed()) {
                openConnection();
            }
            Connection connection = getConnection();
            String sqlcreateTable = "CREATE TABLE IF NOT EXISTS commands(commandID varchar(200), command varchar(1000));";
            String insertData = "INSERT INTO commands VALUE (?, ?);";

            // Create table
            PreparedStatement stmt = connection.prepareStatement(sqlcreateTable);
            stmt.executeUpdate();

            PreparedStatement insertDataStatement = connection.prepareStatement(insertData);
            insertDataStatement.setString(1, UUID.randomUUID().toString());
            insertDataStatement.setString(2, command);
            insertDataStatement.executeUpdate();
            closeConnection();
        }
        catch (SQLException e){
            System.out.println("A database error has occurred!");
            closeConnection();
            e.printStackTrace();
        }
    }
    public ArrayList<String> getConsoleMessages(){
        final ArrayList<String> consoleMessages = new ArrayList<>();
        try {
            this.lastDatabaseConnection = System.currentTimeMillis();
            if (getConnection() == null || getConnection().isClosed()) {
                openConnection();
            }
            Connection connection = getConnection();
            String sqlcreateTable = "CREATE TABLE IF NOT EXISTS logs(logID integer(100), logMessage varchar(10000));";
            String getLogs = "SELECT * FROM logs ORDER BY logID;";
            String deleteLog = "DELETE FROM logs WHERE logID = ?";

            // Create table
            PreparedStatement stmt = connection.prepareStatement(sqlcreateTable);
            stmt.executeUpdate();

            PreparedStatement getCommandStatement = connection.prepareStatement(getLogs);
            ResultSet results = getCommandStatement.executeQuery();
            while(results.next()){
                consoleMessages.add(results.getString(2));

                PreparedStatement deleteLogs = connection.prepareStatement(deleteLog);
                deleteLogs.setInt(1, results.getInt(1));
                deleteLogs.executeUpdate();
            }
        }
        catch (SQLException e){
            System.out.println("A database error has occurred!");
            closeConnection();
            e.printStackTrace();
        }
        return consoleMessages;
    }

}
