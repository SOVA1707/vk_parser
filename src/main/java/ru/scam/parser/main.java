package ru.scam.parser;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;


public class main {

    public static String token = "";
    public static int userId = 0;

    public static UserActor user;
    public static VkApiClient vk;
    final public static int count = 200;
    final public static int repeat = 1000000;
    final public static String folder_path = "C:\\VKParser\\";
    public static int skip = 0;

    public static void main(String[] args){

        if (args.length > 0) {
            skip = Integer.parseInt(args[0]);
        }

        if (args.length > 2) {
            token = args[1];
            userId = Integer.parseInt(args[2]);

        }

        user = new UserActor(userId, token);

        TransportClient transportClient = new HttpTransportClient();
        vk = new VkApiClient(transportClient);

        ParsMessages.parsMessages(skip);
        ParsAlbums.parsAlbums();
        System.out.println("---Parse current user page---");
        ParsPage.parseUser(folder_path, String.valueOf(userId));
        System.out.println("---End parse current user page---");
        System.out.println("---Parse friends info---");
        ParsFriends.parseFriends();
        System.out.println("---End parse friends---");
        ParsDocs.parsDocs();
    }
}