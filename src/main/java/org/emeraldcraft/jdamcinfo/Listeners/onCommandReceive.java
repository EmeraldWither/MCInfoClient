package org.emeraldcraft.jdamcinfo.Listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import org.emeraldcraft.jdamcinfo.Bot;
import org.emeraldcraft.jdamcinfo.ServerInfo;

import java.awt.*;

public class onCommandReceive extends ListenerAdapter {

    long lastRanCommand;
    ServerInfo serverInfo;

    @Override
    public void onSlashCommand(SlashCommandEvent event){
        if(event.getSubcommandName() == null){
            event.reply("Please provide a subcommand!").setEphemeral(true).queue();
            return;
        }
        if(event.getSubcommandName().equalsIgnoreCase("execute")){
            int cache;
            try {
                cache = Integer.parseInt(Bot.getConfig().getProperty("cache.length"));
            } catch (NumberFormatException e) {
                System.out.println("cache.length was unable to be parsed. Defaulting to 10 seconds. ");
                cache = 10;
            }
            if (!(lastRanCommand > System.currentTimeMillis())) {
                serverInfo = Bot.getDatabase().getServerInfo();
            }
            if (serverInfo.isOnline()) {
                return;
            }
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor(serverInfo.getServerName());
            embedBuilder.setTitle(":no_entry_sign:  Sorry, but the server is currently offline! :no_entry_sign: ");
            embedBuilder.setFooter("Results may be cached for up to " + cache + " seconds. \nMade by EmerqldWither");
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            lastRanCommand = (System.currentTimeMillis() + (cache * 1000L));

        }
        if(event.getSubcommandName().equalsIgnoreCase("info")) {
            EmbedBuilder messageEmbed = new EmbedBuilder();
            int cache;
            try {
                cache = Integer.parseInt(Bot.getConfig().getProperty("cache.length"));
            } catch (NumberFormatException e) {
                System.out.println("cache.length was unable to be parsed. Defaulting to 10 seconds. ");
                cache = 10;
            }
            if (!(lastRanCommand > System.currentTimeMillis())) {
                serverInfo = Bot.getDatabase().getServerInfo();
            }

            messageEmbed.setAuthor(serverInfo.getServerName());
            if (serverInfo.isOnline()) {
                messageEmbed.setColor(Color.GREEN);
                messageEmbed.setDescription(
                                "Server Status : **Online**\n" +
                                "Online Players : **" + serverInfo.getOnlinePlayers() + "/" + serverInfo.getMaxPlayers() + "**\n" +
                                "TPS : **" + serverInfo.getTps() + "**\n" +
                                "Version : **" + serverInfo.getMcVersion() + "**");
            } else {
                messageEmbed.setColor(Color.RED);
                messageEmbed.setDescription(
                                "Server Status : **Offline**\n" +
                                "Last Known Version : **" + serverInfo.getMcVersion() + "**\n" +
                                "Last Known Max Players: **" + serverInfo.getMaxPlayers() + "**");
            }
            messageEmbed.setFooter("Results may be cached for up to " + cache + " seconds. \nMade by EmerqldWither");
            event.replyEmbeds(messageEmbed.build()).setEphemeral(false).addActionRow(
                    Button.link("https://github.com/EmeraldWither/McINFO_Standalone", "Link to Github")).queue();
            lastRanCommand = (System.currentTimeMillis() + (cache * 1000L));
        }
        if(event.getSubcommandName().equalsIgnoreCase("about")){
            EmbedBuilder messageEmbed = new EmbedBuilder();
            event.replyEmbeds(messageEmbed.build()).setEphemeral(false).queue();
        }
    }
}
