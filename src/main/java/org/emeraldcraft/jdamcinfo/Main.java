package org.emeraldcraft.jdamcinfo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.emeraldcraft.jdamcinfo.DatabaseManagers.Database;
import org.emeraldcraft.jdamcinfo.Listeners.onCommandReceive;

import java.io.File;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) {
        try {
            //Load file configuration
            File f = new File(System.getProperty("java.class.path"));
            File dir = f.getAbsoluteFile().getParentFile();
            String path = dir.toString();
            new BotConfig().createConfig(path);
            System.out.println("Created config. Now starting up the bot");

            if (Bot.getConfig().getProperty("bot.token").equalsIgnoreCase("bottokenhere")) {
                System.out.println("Please input the bot token!");
                System.out.println("Error! Could not login! Shutting down in 10 seconds.");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("Shutting down now. ");
                System.exit(0);
                return;
            }
            String url = Bot.getConfig().getProperty("db.url");
            String stringPort = Bot.getConfig().getProperty("db.port");
            String dbname = Bot.getConfig().getProperty("db.name");
            String username = Bot.getConfig().getProperty("db.user");
            String password = Bot.getConfig().getProperty("db.password");

            int port = Integer.parseInt(stringPort);
            Database database = new Database(url, port, dbname, username, password);
            JDABuilder builder = JDABuilder.createDefault(Bot.getConfig().getProperty("bot.token"));
            builder.setStatus(OnlineStatus.ONLINE);
            builder.setActivity(Activity.listening("/mcserver"));
            JDA bot = builder.build();
            bot.awaitReady();
            bot.addEventListener(new onCommandReceive());
            bot.retrieveCommands().queue(commands -> {
                boolean foundCommand = false;
                for (Command command : bot.retrieveCommands().complete()) {
                    if (command.getName().equalsIgnoreCase("mcserver")) {
                        foundCommand = true;
                    }
                }
                if (!foundCommand) {
                    System.out.println("Had to upsert a command.");
                    Bot.getBot().upsertCommand("mcserver", "Minecraft Server command.")
                            .addSubcommands(new SubcommandData("info", "Get information about the minecraft server."))
                            .addSubcommands(new SubcommandData("execute", "Execute a minecraft server command").addOption(OptionType.STRING, "command", "The command that you want to run", true))
                            .queue();
                }
            });


            Bot Bot;
            Bot = new Bot();
            Bot.setJda(bot);
            Bot.setDatabase(database);
            CompletableFuture.runAsync(() -> {
                try {
                    database.testConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                    shutdown();
                }
            });
            CompletableFuture.runAsync(() -> {
                ServerInfo serverInfo = database.getServerInfo();
                System.out.println("Current database information: ");
                System.out.println("IsOnline = " + serverInfo.isOnline());
                System.out.println("Minecraft Server = " + serverInfo.getMcVersion());
                System.out.println("Online Players = " + serverInfo.getOnlinePlayers());
                System.out.println("Max Players = " + serverInfo.getMaxPlayers());
                System.out.println("TPS = " + serverInfo.getTps());
                checkCommand();
            });



        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }

    }

    public static void shutdown() {
        System.out.println("Shutting down now");
        if (Bot.getBot() != null) {
            Bot.getBot().getPresence().setStatus(OnlineStatus.IDLE);
            Bot.getBot().shutdownNow();
        }
        if (Bot.getDatabase() != null) {
            Bot.getDatabase().closeConnection();
        }
        System.exit(0);
    }

    public static void checkCommand() {
        System.out.print("If you wish to stop the bot, type \"stop\" here. If you wish to attempt to upsert the /mcserver command, type  \"upsertcommand\" here.");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("stop")) {
            shutdown();
            return;
        }
        if (response.equalsIgnoreCase("upsertcommand")) {
            System.out.println("Attempting to upsert the command. ");
            Bot.getBot().upsertCommand("mcserver", "Minecraft Server command.")
                    .addSubcommands(new SubcommandData("info", "Get information about the minecraft server."))
                    .addSubcommands(new SubcommandData("execute", "Execute a minecraft server command").addOption(OptionType.STRING, "command", "The command that you want to run", true))
                    .queue();
            System.out.println("Queued the upsert command.");
            checkCommand();
            return;
        }
        System.out.println("Unknown Command. Try again:");
        checkCommand();
    }
}

