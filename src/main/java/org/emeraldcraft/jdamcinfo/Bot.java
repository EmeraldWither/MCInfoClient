package org.emeraldcraft.jdamcinfo;

import net.dv8tion.jda.api.JDA;
import org.emeraldcraft.jdamcinfo.DatabaseManagers.Database;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class Bot {

    private static JDA jda;
    private static Database database;

    /**
     * @return The JDA bot instance.
     */
    public static JDA getBot(){
     return jda;
    }

    /**
     * @return The configuration file. Note that it does NOT return a new properties file.
     */
    public static Properties getConfig(){
        return BotConfig.getConfig();
    }

    public static Database getDatabase() {
        return database;
    }

    /**
     * @param jda Only used for internal use
     */
    public void setJda(@NotNull JDA jda) {
        if(Bot.jda == null){
            Bot.jda = jda;
        }
    }

    /**
     * @param database Only for internal use.
     */
    public void setDatabase(@NotNull Database database) {
        if(Bot.database == null){
            Bot.database = database;
        }
    }
}
