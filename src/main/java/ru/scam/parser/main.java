package ru.scam.parser;

import com.vk.api.sdk.actions.Account;
import com.vk.api.sdk.actions.Friends;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import static ru.scam.parser.ParsMessages.parsMessages;

public class main {

    public static String token = "";
    public static int userId = 0;

    public static UserActor user;
    public static VkApiClient vk;
    final public static int count = 200;
    final public static int repeat = 100000;
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

//        parsMessages(skip);
//        ParsAlbums.parsAlbums();
//        ParsPage.parsUserPage();
//        ParsFriends.parseFriends();
    }
}