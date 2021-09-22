package org.emeraldcraft.jdamcinfo;

public class ServerInfo {

    private final boolean isOnline;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final int tps;
    private final String mcVersion;
    private String motd;
    private String serverName;

    /**
     * @param isOnline boolean value if server is online
     * @param onlinePlayers how many players are online
     * @param maxPlayers how many max players there can be
     * @param tps tps of the server
     * @param mcVersion mc version. (ex: 1.17)
     * @deprecated MOTD - Server will no longer provide an MOTD. The default value will be blank.
     */
    @Deprecated(forRemoval = true)
    public ServerInfo(boolean isOnline, int onlinePlayers, int maxPlayers, int tps, String mcVersion){

        this.isOnline = isOnline;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.tps = tps;
        this.mcVersion = mcVersion;
        this.motd = motd;
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

    public ServerInfo(boolean isOnline, int onlinePlayers, int maxPlayers, int tps, String mcVersion, String serverName) {
        this.isOnline = isOnline;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.tps = tps;
        this.mcVersion = mcVersion;
        this.serverName = serverName;
        motd = "";
    }

    public String getMcVersion() {
        return mcVersion;
    }

    @Deprecated
    public String getMotd() {
        return motd;
    }

    public String getServerName() {
        return serverName;
    }
}
