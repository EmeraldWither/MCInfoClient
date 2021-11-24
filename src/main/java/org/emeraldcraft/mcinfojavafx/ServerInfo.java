package org.emeraldcraft.mcinfojavafx;

public class ServerInfo {
    private final boolean isOnline;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final int tps;
    private final String mcVersion;
    private final String serverName;
    public ServerInfo(boolean isOnline, int onlinePlayers, int maxPlayers, int tps, String mcVersion, String serverName) {
        this.isOnline = isOnline;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.tps = tps;
        this.mcVersion = mcVersion;
        this.serverName = serverName;
    }
    public boolean isOnline() {
        return isOnline;
    }
    public int getOnlinePlayers() {
        return onlinePlayers;
    }
    public int getMaxPlayers() {
        return maxPlayers;
    }
    public int getTps() {
        return tps;
    }
    public String getMcVersion() {
        return mcVersion;
    }
    public String getServerName() {
        return serverName;
    }
}
