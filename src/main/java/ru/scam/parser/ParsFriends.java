package ru.scam.parser;

import com.vk.api.sdk.actions.Friends;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.friends.responses.GetResponse;

public class ParsFriends {
    final static VkApiClient vk = main.vk;
    final static UserActor user = main.user;
    final static int count = main.count;
    final static int repeat = main.repeat;
    final static String folder_path = main.folder_path + "friends\\";

    public static void parseFriends() {
        Friends friends = new Friends(vk);

        for (int i = 0; i < repeat; i++) {
            try {
                GetResponse r = friends.get(user).offset(i * count).count(count).execute();
                if (r.getItems().size() == 0) break;
                for (Integer id : r.getItems()) {
                    System.out.println(id);
                }
            } catch (ApiException | ClientException e) {
                System.out.println("Error 367...");
                e.printStackTrace();
                ParsMessages.smallSleep();
                parseFriends();
            }
        }
    }
}
