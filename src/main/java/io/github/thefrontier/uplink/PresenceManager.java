package io.github.thefrontier.uplink;

import club.minnced.discord.rpc.DiscordRichPresence;
import cpw.mods.fml.common.Loader;
import io.github.thefrontier.uplink.config.Config;
import io.github.thefrontier.uplink.config.DisplayDataManager;
import io.github.thefrontier.uplink.config.display.ServerDisplay;
import io.github.thefrontier.uplink.config.display.SmallDisplay;
import io.github.thefrontier.uplink.util.MiscUtil;


class PresenceManager {

    private final DisplayDataManager dataManager;
    private final Config config;

    private final DiscordRichPresence loadingGame = new DiscordRichPresence();
    private final DiscordRichPresence mainMenu = new DiscordRichPresence();
    private final DiscordRichPresence inGame = new DiscordRichPresence();

    public static final long startTime;

    static{
        startTime = MiscUtil.epochSecond();
    }

    private PresenceState curState = PresenceState.INIT;

    PresenceManager(DisplayDataManager dataManager, Config config) {
        this.dataManager = dataManager;
        this.config = config;

        loadingGame.state = "Загружает игру";
        loadingGame.largeImageKey = "state-load";
        loadingGame.largeImageText = "Minecraft";

        mainMenu.state = " В главном меню";
        mainMenu.largeImageKey = "state-menu";
        mainMenu.largeImageText = "Main Menu";

        SmallDisplay smallData = dataManager.getSmallDisplays().get(this.config.smallDataUid);

        if (smallData == null) {
            return;
        }

        loadingGame.smallImageKey = smallData.getKey();
        loadingGame.smallImageText = smallData.getName();

        mainMenu.smallImageKey = smallData.getKey();
        mainMenu.smallImageText = smallData.getName();

        inGame.smallImageKey = smallData.getKey();
        inGame.smallImageText = smallData.getName();
    }

    // ------------------- Getters -------------------- //

    public PresenceState getCurState() {
        return curState;
    }

    public void setCurState(PresenceState curState) {
        this.curState = curState;
    }

    public DisplayDataManager getDataManager() {
        return dataManager;
    }

    public Config getConfig() {
        return config;
    }

    // -------------------- Mutators -------------------- //

    DiscordRichPresence loadingGame() {
        int mods = (int) Loader.instance().getModList().stream().count();
        loadingGame.startTimestamp = startTime;
        loadingGame.details = String.format("With %d mods", mods);
        return loadingGame;
    }

    DiscordRichPresence mainMenu() {
        mainMenu.startTimestamp = startTime;

        return mainMenu;
    }

    DiscordRichPresence ingameMP(String ip, int playerCount, int maxPlayers) {
        ServerDisplay server = dataManager.getServerDisplays().get(ip);

        if (server != null) {
            inGame.largeImageKey = server.getKey();
            inGame.largeImageText = server.getName();
        } else if (this.config.hideUnknownIPs) {
            inGame.largeImageKey = "state-unknown-server";
            inGame.largeImageText = "Неизвестный сервер";
        } else {
            inGame.largeImageKey = "state-unknown-server";
            inGame.largeImageText = "IP: " + ip;
        }

        inGame.state = inGame.largeImageText;
        inGame.details = "Ник: " + MiscUtil.getIGN();
        inGame.startTimestamp = startTime;
        inGame.partyId = ip;
        inGame.partySize = playerCount;
        inGame.partyMax = maxPlayers;

        return inGame;
    }

    DiscordRichPresence updatePlayerCount(int playerCount, int maxPlayers) {
        inGame.partySize = playerCount;
        inGame.partyMax = maxPlayers;

        return inGame;
    }

    DiscordRichPresence ingameSP(String world) {
        inGame.state = "В одиночной игре";
        inGame.details = "Ник: " + MiscUtil.getIGN();
        inGame.startTimestamp = startTime;
        inGame.largeImageKey = "state-singleplayer";
        inGame.partyId = "";
        inGame.partySize = 0;
        inGame.partyMax = 0;

        return inGame;
    }
}