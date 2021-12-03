package org.emeraldcraft.mcinfojavafx.JavaFX;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.emeraldcraft.mcinfojavafx.Bot;
import org.emeraldcraft.mcinfojavafx.ServerInfo;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class GUIController  {
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
    @FXML
    private TextField executeCommandTextField;
    @FXML
    private Button executeCommandButton;
    @FXML
    private Label executeCommandResultLabel;
    @FXML
    private TextArea consoleTextArea;
    @FXML
    private Label versionLabel;

    private Timer currentTimer;

    public void updateInformation() throws SQLException {
        versionLabel.setText("v" + Bot.PROGRAM_VERSION);
        consoleTextArea.setEditable(false);
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
            executeCommandButton.setDisable(false);
            executeCommandTextField.setDisable(false);
            return;
        }
        serverOnlineStatusLabel.setText("OFFLINE");
        serverOnlineStatusLabel.setTextFill(Color.RED);
        executeCommandButton.setDisable(true);
        executeCommandTextField.setDisable(true);
    }
    public void executeCommand() throws SQLException {
        if(executeCommandTextField.getText().isEmpty()){
            if(currentTimer != null) currentTimer.cancel();
            executeCommandResultLabel.setText("Please enter in a command.");
            executeCommandResultLabel.setTextFill(Color.RED);
            currentTimer = new Timer();
            currentTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> executeCommandResultLabel.setText(""));
                }
            }, 10 * 1000);
            return;
        }
        executeCommandButton.setDisable(false);
        ServerInfo serverInfo = Bot.getDatabase().getServerInfo();
        if(!serverInfo.isOnline()){
            if(currentTimer != null) currentTimer.cancel();
            executeCommandTextField.setDisable(false);

            executeCommandResultLabel.setText("The server is not online!");
            executeCommandResultLabel.setTextFill(Color.RED);
            updateInformation();

            currentTimer = new Timer();
            currentTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> executeCommandResultLabel.setText(""));
                }
            }, (10) * 1000);
            return;
        }
        CompletableFuture.runAsync(() -> {
            Bot.getDatabase().queueCommand(executeCommandTextField.getText());
            Platform.runLater(() -> {
                if(currentTimer != null) currentTimer.cancel();
                executeCommandButton.setDisable(false);
                executeCommandTextField.setText("");
                executeCommandResultLabel.setText("Queued the command to be executed!");
                executeCommandResultLabel.setTextFill(Color.GREEN);
                currentTimer = new Timer();
                currentTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> executeCommandResultLabel.setText(""));
                    }
                }, (10) * 1000);
            });
        });
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
    public void updateConsoleMessages(){
        consoleTextArea.textProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> consoleTextArea.setScrollTop(Double.MAX_VALUE));
        final ArrayList<String> consoleMessages = Bot.getDatabase().getConsoleMessages();
        for(String consoleMsg : consoleMessages){
            consoleTextArea.appendText(consoleMsg + "\n");
        }
    }
}