package org.emeraldcraft.mcinfojavafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.emeraldcraft.mcinfojavafx.JavaFX.GUIController;
import org.emeraldcraft.mcinfojavafx.Listeners.onCommandReceive;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    private static Stage stage;
    private static boolean isIncorrect = false;
    @Override
    public void start(Stage stage) throws IOException {
        Main.stage = stage;
        FXMLLoader fxmlLoader;
        Scene scene;
        if(isIncorrect){
            fxmlLoader = new FXMLLoader(Main.class.getResource("DiscordStartupErrorScene.fxml"));
            scene = new Scene(fxmlLoader.load(), 700, 400);
        }
        else {
            fxmlLoader = new FXMLLoader(Main.class.getResource("BotInitializeScene.fxml"));
            scene = new Scene(fxmlLoader.load(), 604, 400);
        }
        stage.setTitle("Discord Bot");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new Image("loading.png"));
        stage.show();
    }

    public static void main(String[] args) {
        CompletableFuture.runAsync(() ->{
            try {
                //Load file configuration
                File f = new File(System.getProperty("java.class.path"));
                File dir = f.getAbsoluteFile().getParentFile();
                String path = dir.toString();
                Bot.createConfig(path);
                System.out.println("Created config. Now starting up the bot");

                if (Bot.getConfig().getProperty("bot.token").equalsIgnoreCase("bottokenhere")) {
                    System.out.println("Please input the bot token!");
                    System.out.println("Error! Could not login! Shutting down in 10 seconds.");
                    isIncorrect = true;
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
                boolean foundCommand = false;
                for (Command command : bot.retrieveCommands().complete()) {
                    if (command.getName().equalsIgnoreCase("mcserver")) {
                        foundCommand = true;
                        break;
                    }
                }
                if (!foundCommand) {
                    System.out.println("Had to upsert a command.");
                    Bot.getBot().upsertCommand("mcserver", "Minecraft Server command.")
                            .addSubcommands(new SubcommandData("info", "Get information about the minecraft server."))
                            .addSubcommands(new SubcommandData("execute", "Execute a minecraft server command").addOption(OptionType.STRING, "command", "The command that you want to run", true))
                            .queue();
                }
                Bot Bot;
                Bot = new Bot();
                Bot.setJda(bot);
                Bot.setStage(stage);
                Bot.setDatabase(database);
                CompletableFuture.runAsync(() -> {
                    try {
                        database.testConnection();
                        ServerInfo serverInfo = database.getServerInfo();
                        System.out.println("Current database information: ");
                        System.out.println("IsOnline = " + serverInfo.isOnline());
                        System.out.println("Minecraft Server = " + serverInfo.getMcVersion());
                        System.out.println("Online Players = " + serverInfo.getOnlinePlayers());
                        System.out.println("Max Players = " + serverInfo.getMaxPlayers());
                        System.out.println("TPS = " + serverInfo.getTps());
                        checkCommand();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        shutdown();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                shutdown();
            }
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("BotControlPanel.fxml"));
            Platform.runLater(() -> {
                try {
                    Scene scene = new Scene(fxmlLoader.load(), 603, 400);
                    stage.setResizable(false);
                    stage.setOnCloseRequest(windowEvent -> shutdown());
                    stage.getIcons().clear();
                    stage.getIcons().add(new Image("icon.png"));
                    Bot.getStage().setScene(scene);
                    Timer timer = new Timer();
                    GUIController controller = fxmlLoader.getController();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(controller == null){
                                System.out.println("THE CONTROLLER IS NULL!");
                                return;
                            }
                            Platform.runLater(() -> {
                                try {
                                    Bot.getDatabase().openConnection();
                                    controller.updateInformation();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }, 0, 5000);
                    stage.setOnCloseRequest(windowEvent -> {
                        windowEvent.consume();
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Are you sure you want to close?");
                        alert.setHeaderText("Are you sure you want to shutdown the bot?");
                        alert.setContentText("Doing so will result in the bot going offline.");
                        if(alert.showAndWait().get() == ButtonType.OK){
                            shutdown();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        launch(args);


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
        System.out.println("The program has successfully shut down.");
        System.exit(0);
    }

    public static void checkCommand() {
        System.out.println("If you wish to stop the bot, type \"stop\" here. If you wish to attempt to upsert the /mcserver command, type  \"upsertcommand\" here.");
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