package ru.scam.parser;

import com.vk.api.sdk.actions.Account;
import com.vk.api.sdk.actions.Friends;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import static ru.scam.parser.ParsMessages.parsMessages;

public class main {

    public static String token = "8ba1d60bd21bff159d0850ad71d59afae5ec0b86023f7916431887510b4f04621bab852c170df08460d2b";
    public static int userId = 134891922;
    public static UserActor user = new UserActor(userId, token);
    final public static int count = 200;
    final public static int repeat = 1000;
    final public static String folder_path = "C:\\VKParser\\";

    public static void main(String[] args) throws Exception {

        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);


        Account account = new Account(vk);
        Friends friends = new Friends(vk);

//        parsMessages(vk, user);
        ParsAlbums.parsAlbums(vk, user);
    }

}
