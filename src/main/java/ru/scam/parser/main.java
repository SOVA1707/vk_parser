package ru.scam.parser;

import com.vk.api.sdk.actions.Account;
import com.vk.api.sdk.actions.Friends;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import static ru.scam.parser.ParsMessages.parsMessages;

public class main {

    public static String token = "d028ab3a09e72d9160e4db865397d2f68f102c00608cfbb6edcc8e174464dc1a6cbb0f924d59f915aac8a";
    //d028ab3a09e72d9160e4db865397d2f68f102c00608cfbb6edcc8e174464dc1a6cbb0f924d59f915aac8a
    public static int userId = 207679023; //63876088 - Г Т Е
    //207679023
    public static UserActor user;
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
        VkApiClient vk = new VkApiClient(transportClient);

//        Friends friends = new Friends(vk);

//        parsMessages(vk, user, skip);
//        ParsAlbums.parsAlbums(vk, user);
//        ParsPage.parsUserPage(vk, user);
//        ParsFriends.parseFriends(vk, user);
    }
}