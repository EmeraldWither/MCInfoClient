package org.emeraldcraft.mcinfojavafx.Github;

import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static org.emeraldcraft.mcinfojavafx.Bot.GITHUB_REPO_ID;
import static org.emeraldcraft.mcinfojavafx.Bot.PROGRAM_VERSION;

public class AutoUpdater {
    public static GHRelease hasUpdate() throws IOException {
        String currentVersionModified = PROGRAM_VERSION.replace("v", "").replaceAll("\\.", "").replaceAll("-STABLE", "");
        if(PROGRAM_VERSION.split("-")[1].equalsIgnoreCase("DEV")) return null;
        GitHub gitHub = GitHub.connectAnonymously();
        if (gitHub.getRateLimit().getRemaining() <= 5) {
            System.out.println("I could not contact GitHub because we have been rate-limited. Will try again later");
            return null;
        }
        System.out.println("Connected to GitHub successfully.");
        GHRepository repository = gitHub.getRepositoryById(GITHUB_REPO_ID);
        GHRelease release = repository.getLatestRelease();
        if(release == null){
            System.out.println("There are no releases to download (latest release is null).");
            return null;
        }
        if(release.isPrerelease()){
            System.out.println("There is no releases to downloads (latest is pre-release).");
            return null;
        }
        String tag = release.getTagName();
        if(!tag.endsWith("-javafx")){
            System.out.println("There are no releases to download (no valid tag found).");
            return null;
        }

        String tagModified = tag.replaceAll("\\.", "").replaceAll("-javafx", "");

        int tagInt = Integer.parseInt(tagModified);
        int currentVersion = Integer.parseInt(currentVersionModified);
        if (tagInt <= currentVersion) {
            System.out.println("We are on the latest version (" + currentVersion + ")!");
            return null;
        }
        return release;

    }
    public static void downloadUpdates(){
        try {
            GitHub gitHub = GitHub.connectAnonymously();
            if (gitHub.getRateLimit().getRemaining() <= 5) {
                System.out.println("I could not contact GitHub because we have been rate-limited. Will try again later");
                return;
            }
            GHRelease release = gitHub.getRepositoryById(GITHUB_REPO_ID).getLatestRelease();
            if(release == null){
                System.out.println("There are no releases to download (latest release is null).");
                return;
            }
            if(release.isPrerelease()){
                System.out.println("There is no releases to downloads (latest is pre-release).");
            }
            String tag = release.getTagName();
            if(!tag.endsWith("-javafx")){
                System.out.println("There are no releases to download (no valid tag found).");
                return;
            }

            List<GHAsset> files = release.listAssets().toList();
            for (GHAsset asset : files) {
                String[] possibleExtensions = asset.getName().split("\\.");
                String extension = possibleExtensions[possibleExtensions.length - 1];
                if (extension.equalsIgnoreCase("jar")) {
                    String downloadLocation = System.getProperty("user.dir") + "/" + asset.getName();
                    String runScriptLocation = System.getProperty("user.dir") + "/run.bat";

                    downloadFile(new URL(asset.getBrowserDownloadUrl()), downloadLocation);
                    overWriteStartScript(runScriptLocation, asset.getName());
                    break;
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void downloadFile(URL url, String fileName) {
        try (InputStream in = url.openStream()) {
            File file = new File(fileName);
            if (file.exists()) return;

            System.out.println("Downloading file to: " + Paths.get(fileName));
            Files.copy(in, Paths.get(fileName));
        } catch (IOException e) {
            System.out.println("I was unable to download the file " + fileName + " from " + url);
            e.printStackTrace();
        }
    }

    private static void overWriteStartScript(String path, String fileName) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Start script does not exist. Will not overwrite");
                return;
            }

            Scanner scanner = new Scanner(file);
            if (scanner.hasNextLine()) {
                FileWriter writer = new FileWriter(file);
                String line = scanner.nextLine();
                if (line.isBlank()) {
                    writer.close();
                    scanner.close();
                    return;
                }
                String s = line.split("-jar")[1];
                String previousArgs = line.split("-jar")[0] + "-jar ";
                String afterArgs = "\"";
                if (s.split("\"").length >= 3) {
                    afterArgs = "\"" + s.split("\"")[2];
                }
                String writeData = previousArgs + "\"" + fileName + afterArgs;
                writer.write(writeData);
                writer.close();
                scanner.close();
                System.out.println("Overwrote the start script at " + path);
            }
        } catch (IOException e) {
            System.out.println("I was unable to overwrite the start script located at " + path);
            e.printStackTrace();
        }
    }
}
