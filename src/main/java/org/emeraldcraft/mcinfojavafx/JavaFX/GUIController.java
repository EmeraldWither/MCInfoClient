package org.emeraldcraft.mcinfojavafx.JavaFX;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.emeraldcraft.mcinfojavafx.Bot;
import org.emeraldcraft.mcinfojavafx.ServerInfo;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class GUIController {
    @FXML
    private Label serverTPSLabel;
    @FXML
    private Label playerCountLabel;
    @FXML
    private Label serverVersionLabel;
    @FXML
    private Label serverOnlineStatusLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private Label lastDatabaseConnectionLabel;

    public void updateInformation() throws SQLException {
        usernameLabel.setText("Currently logged in as: " + Bot.getBot().getSelfUser().getAsTag());
        ServerInfo serverInfo;
        if(Bot.getDatabase().getCachedServerInfo() == null){
           serverInfo = Bot.getDatabase().getServerInfo();
        }
        else {
            serverInfo = Bot.getDatabase().getCachedServerInfo();
        }
        serverTPSLabel.setText("" + serverInfo.getTps());
        serverVersionLabel.setText("" + serverInfo.getMcVersion());
        playerCountLabel.setText(serverInfo.getOnlinePlayers() + "/" + serverInfo.getMaxPlayers());

        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(Bot.getDatabase().lastDatabaseConnection()), ZoneId.systemDefault());
        String formattedDate = date.format(DateTimeFormatter.ofPattern("M/dd/uu K:mm:ss a"));
        lastDatabaseConnectionLabel.setText(formattedDate);

        Bot.getStage().setTitle(serverInfo.getServerName() + "'s Discord Bot || Made by EmerqldWither");

        if(serverInfo.isOnline()){
            serverOnlineStatusLabel.setText("ONLINE");
            serverOnlineStatusLabel.setTextFill(Color.GREEN);
            return;
        }
        serverOnlineStatusLabel.setText("OFFLINE");
        serverOnlineStatusLabel.setTextFill(Color.RED);
    }
    public void invalidateCaches(){
        Platform.runLater(() -> {
            refreshButton.setDisable(true);
            CompletableFuture.runAsync(() -> {
                Bot.getDatabase().openConnection();
                Bot.getDatabase().setCachedServerInfo(Bot.getDatabase().getServerInfo());
                Platform.runLater(() -> {
                    try {
                        updateInformation();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    refreshButton.setDisable(false);
                });
            });
        });
    }
}