package ru.scam.parser;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import ru.scam.parser.calc.Tool;


public class main {

    public static String token = "ebbdd3c56dc922e16ebf84eace2c8b44c262d8964909d9cc4b12ea0747929309f0fc18b862de968327d05";
    public static int userId = 207679023;

    public static UserActor user;
    public static VkApiClient vk;
    public static final int COUNT = 200;
    public static final int REPEAT = 1000000;
    public static String FOLDER_PATH = "C:\\VKParser\\";
    public static final String FARM_PATH = FOLDER_PATH + "FarmCoin\\";
    public static int skip = 0;

    public static void main(String[] args) {
        initToken(args);
        FOLDER_PATH += userId + "\\";

//        Parse();

        FarmCoin.Farm(12);

    }

    public static void Parse() {
        ParsMessages.parsMessages(skip);
        ParsAlbums.parsAlbums();
        System.out.println("---Parse current user page---");
        ParsPage.parseUser(FOLDER_PATH, String.valueOf(userId));
        System.out.println("---End parse current user page---");
        System.out.println("---Parse friends info---");
        ParsFriends.parseFriends();
        System.out.println("---End parse friends---");
        ParsDocs.parsDocs();
    }

    private static void initToken(String[] args) {
        if (args.length > 0) {
            skip = Integer.parseInt(args[0]);
        }

        if (args.length > 2) {
            token = args[1];
            userId = Integer.parseInt(args[2]);
        }

        user = new UserActor(userId, token);
        vk = new VkApiClient(new HttpTransportClient());
    }
}