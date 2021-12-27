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
    final public static int count = 200;
    final public static int repeat = 100000;
    final public static String folder_path = "C:\\VKParser\\";

    public static void main(String[] args) throws Exception {

        int skip = 0;
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


        Account account = new Account(vk);
        Friends friends = new Friends(vk);

        parsMessages(vk, user, skip);
//        ParsAlbums.parsAlbums(vk, user);
    }

}
