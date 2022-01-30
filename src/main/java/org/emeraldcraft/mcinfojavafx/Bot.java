package org.emeraldcraft.mcinfojavafx;

import javafx.stage.Stage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class Bot {

    private static JDA jda;
    private static Database database;
    private static Stage stage;
    public static final String PROGRAM_VERSION = "1.1.5-DEV";
    public static final String AUTHOR = "EmerqldWither";
    public static final long GITHUB_REPO_ID = 409025455;
    public static final Long START_TIME = System.currentTimeMillis();
    private static Stage updateStage;

    /**
     * @return The JDA bot instance.
     */
    public static JDA getBot(){
     return jda;
    }
    public static void shutdown(boolean restart) {
        System.out.println("Shutting down now");
        if (Bot.getBot() != null) {
            Bot.getBot().getPresence().setStatus(OnlineStatus.IDLE);
            Bot.getBot().shutdownNow();
        }
        if (Bot.getDatabase() != null) {
            Bot.getDatabase().closeConnection();
        }
        System.out.println("The program has successfully shut down.");
        if(restart){
            System.out.println("Restarting...");
            String runScriptLocation = System.getProperty("user.dir") + "/run.bat";
            File file = new File(runScriptLocation);
            if(file.canExecute()){
                String path = "cmd /c start " + runScriptLocation;
                try {
                    Runtime.getRuntime().exec(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
        System.exit(0);
    }

    public static Database getDatabase() {
        return database;
    }

    /**
     * The stage as per JavaFX
     * @return The JavaFX Scene
     */
    public static Stage getStage() {
        return stage;
    }
    public static Stage getUpdateStage(){
        return updateStage;
    }

    public static void setUpdateStage(Stage updateStage) {
        Bot.updateStage = updateStage;
    }

    public void setStage(Stage stage) {
        Bot.stage = stage;
    }

    /**
     * @param jda Only used for internal use
     */
    public void setJda(JDA jda) {
        Bot.jda = jda;
    }

    /**
     * @param database Only for internal use.
     */
    public void setDatabase(Database database) {
        Bot.database = database;
    }
    private static Properties config;
    /**
     * @return The configuration file. Note that it does NOT return a new properties file.
     */
    public static Properties getConfig() {
        return config;
    }
    public static void createConfig(String path){
        File file = new File(path + "/config.properties");
        Properties sortedProperties  = new SortedProperties();

        // set the properties value
        sortedProperties.setProperty("bot.token", "bottokenhere");
        sortedProperties.setProperty("db.user", "dbuser");
        sortedProperties.setProperty("db.password", "dbpassword");
        sortedProperties.setProperty("db.url", "database.example.com");
        sortedProperties.setProperty("db.name", "jdadb");
        sortedProperties.setProperty("db.port", "3306");
        sortedProperties.setProperty("cache.length", "5");
        sortedProperties.setProperty("console.messages", "false");
        sortedProperties.setProperty("console.messages.optimize", "false");
        String comments;
        comments =
                """
                -------------------------------------------------------------------------------------------------------------------------
                <!> READ ME <!>
                Please note that enabling "console.messages.optimize", will only request when we know that the server is online.
                It does this by using the cached data (you can do this by clicking the refresh button or a user querying the information)
                Keeping this feature disabled will make the program query the database every second.
                
                You can use console.message to disable or enable requesting console messages. (Please use "true" or "false")
                Made by EmerqldWither
                --------------------------------------------------------------------------------------------------------------------------
                """;

        if(file.exists()){
            try (InputStream input = new FileInputStream(path + "/config.properties")) {
                Properties prop = new Properties();
                prop.load(input);
                boolean changed = false;
                for(String key : sortedProperties.stringPropertyNames()) {
                    if(!changed) changed = true;
                    if (prop.getProperty(key) != null && !(prop.getProperty(key).isBlank())) continue;
                    prop.setProperty(key, sortedProperties.getProperty(key));
                }
                if(changed){
                    prop.store(new FileOutputStream(path + "/config.properties"), comments);
                }

                config = prop;

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        try (OutputStream output = new FileOutputStream(path + "/config.properties")) {
            // save properties to project root folder
            sortedProperties.store(output, comments);

            System.out.println("Properties File: " + sortedProperties);
            config = sortedProperties;

        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    private static class SortedProperties extends Properties {
        public Enumeration keys() {
            Enumeration keysEnum = super.keys();
            Vector<String> keyList = new Vector<>();
            while (keysEnum.hasMoreElements()) {
                keyList.add((String) keysEnum.nextElement());
            }
            Collections.sort(keyList);
            return keyList.elements();
        }
    }

}
