package org.emeraldcraft.mcinfojavafx;

public record ServerInfo(boolean isOnline, int onlinePlayers, int maxPlayers, int tps, String mcVersion, String serverName){}
