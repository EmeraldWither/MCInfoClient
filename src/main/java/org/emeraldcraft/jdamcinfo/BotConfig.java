package org.emeraldcraft.jdamcinfo;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class BotConfig {

    private static Properties config;

    public static Properties getConfig() {
        return config;
    }
    public void createConfig(String path){
        File file = new File(path + "/config.properties");
        if(file.exists()){
            try (InputStream input = new FileInputStream(path + "/config.properties")) {

                Properties prop = new Properties();

                // load a properties file
                prop.load(input);

                // get the property value and print it out
                if(!prop.containsKey("cache.length")){
                    prop.setProperty("cache.length", "5");
                }
                config = prop;

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        try (OutputStream output = new FileOutputStream(path + "/config.properties")) {
            SortedProperties prop = new SortedProperties();

            // set the properties value
            prop.setProperty("bot.token", "bottokenhere");
            prop.setProperty("db.user", "dbuser");
            prop.setProperty("db.password", "dbpassword");
            prop.setProperty("db.url", "database.example.com");
            prop.setProperty("db.name", "jdadb");
            prop.setProperty("db.port", "3306");
            prop.setProperty("cache.length", "5");

            // save properties to project root folder
            prop.store(output, null);

            System.out.println("Properties File: " + prop);
            config = prop;

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private class SortedProperties extends Properties {
        public Enumeration keys() {
            Enumeration keysEnum = super.keys();
            Vector<String> keyList = new Vector<String>();
            while (keysEnum.hasMoreElements()) {
                keyList.add((String) keysEnum.nextElement());
            }
            Collections.sort(keyList);
            return keyList.elements();
        }
    }
}
