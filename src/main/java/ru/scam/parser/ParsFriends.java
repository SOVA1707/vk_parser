package ru.scam.parser;

import com.vk.api.sdk.actions.Friends;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.friends.responses.GetResponse;

import static ru.scam.parser.main.*;

public class ParsFriends {
    final static String folder_path = main.FOLDER_PATH + "friends\\";

    public static void parseFriends() {
        Friends friends = new Friends(vk);

        for (int i = 0; i < REPEAT; i++) {
            try {
                GetResponse r = friends.get(user).offset(i * COUNT).count(COUNT).execute();
                if (r.getItems().size() == 0) break;
                for (Integer id : r.getItems()) {
                    ParsPage.parseUser(folder_path, String.valueOf(id));
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
