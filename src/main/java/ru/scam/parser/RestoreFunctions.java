package ru.scam.parser;

import com.vk.api.sdk.actions.Photos;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;

public class RestoreFunctions {
    public static void restorePhotos(VkApiClient vk, UserActor user, int start, int end, int skip) {
        Photos photos = new Photos(vk);

        for (int i = start + skip; i <= end; i++) {
            try {
                if (i % 500 == 0) System.out.println(i);
                photos.restore(user, i).execute();
            } catch (ApiException | ClientException e) {
                System.out.println("Restore error");
                i--;
            }
        }
    }
}
