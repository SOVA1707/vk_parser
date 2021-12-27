package ru.scam.parser;

import com.vk.api.sdk.actions.Photos;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.responses.GetResponse;

import java.util.List;

public class ParsAlbums {
    public static String token = main.token;
    public static int userId = main.userId;
    public static UserActor user = main.user;
    final static int count = main.count;
    final static int repeat = main.repeat;
    final static String folder_path = main.folder_path;
    static int counter = 1;

    public static void parsAlbums(VkApiClient vk, UserActor user) throws ClientException, ApiException {
        Photos photos = new Photos(vk);
        List<PhotoAlbumFull> albums = photos.getAlbums(user).needSystem(true).execute().getItems();
        for (PhotoAlbumFull album : albums) {
            String path = folder_path + album.getTitle() + "\\";
            System.out.println(album.getTitle());
            for (int i = 0; i<repeat; i++) {
                GetResponse phtos = photos.get(user).albumId(String.valueOf(album.getId())).offset(i*count).count(count).execute();
                if (phtos.getItems().size() == 0) break;
                for (Photo p : phtos.getItems()) {
                    try {
                        ParsMessages.downloadFile(ParsMessages.getMaxSizeUrl(ParsMessages.getUrls(p.toString())), path + "image_" + counter);
                    } catch (Exception e) {
                        System.out.println("-----------------");
                        System.out.println(p);
                        System.out.println("-----------------");
                        e.printStackTrace();
                    }
                    counter++;
                }
            }

        }
    }
}
