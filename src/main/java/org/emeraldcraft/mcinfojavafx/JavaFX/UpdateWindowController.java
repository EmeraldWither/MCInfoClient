package org.emeraldcraft.mcinfojavafx.JavaFX;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import org.emeraldcraft.mcinfojavafx.Bot;
import org.emeraldcraft.mcinfojavafx.Github.AutoUpdater;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHRelease;

public class UpdateWindowController {
    @FXML
    private TextArea releaseText;
    @FXML
    private Button ignoreUpdateButton, confirmUpdateButton;
    @FXML
    private Text downloadingText;
    private GUIController controller;

    public void provideController(@NotNull GUIController controller){
        this.controller = controller;
    }
    public void promptUser(GHRelease release){
        controller.setCheckingUpdates(true);
        System.out.println("Now showing the stage.");
        String s =
                """
                        There is a new release of this program!
                        Current Version: %s
                        New Version: %s
                        
                        %s
                        
                        Release Notes:
                        %s
                        """.formatted(Bot.PROGRAM_VERSION, release.getTagName(),release.getName(), release.getBody());
        releaseText.setText(s);
        System.out.println("Now edited release text.");
    }
    public void confirmUpdate(){
        ignoreUpdateButton.setDisable(true);
        confirmUpdateButton.setDisable(true);
        downloadingText.setText("DOWNLOADING...");
        new Thread(() -> {
            AutoUpdater.downloadUpdates();
            Platform.runLater(() -> {
                controller.setCheckingUpdates(false);
                Bot.getUpdateStage().close();
                Bot.shutdown(true);
            });
        }).start();
    }
    public void denyUpdate(){
        ignoreUpdateButton.setDisable(true);
        confirmUpdateButton.setDisable(true);
        controller.setCheckingUpdates(false);
        Bot.getUpdateStage().close();
    }

}
