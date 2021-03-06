package ru.scam.parser;

import com.vk.api.sdk.actions.Photos;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiParamException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoAlbumFull;
import com.vk.api.sdk.objects.photos.responses.GetResponse;

import java.util.List;

import static ru.scam.parser.main.*;

public class ParsAlbums {
    final static String folder_path = main.FOLDER_PATH + "albums\\";
    static int counter = 1;

    public static void parsAlbums() {
        Photos photos = new Photos(vk);
        try {
            List<PhotoAlbumFull> albums = photos.getAlbums(user).needSystem(true).execute().getItems();
            System.out.println("---Parse albums---");
            for (PhotoAlbumFull album : albums) {
                String path = folder_path + album.getTitle() + "\\";
                System.out.println(album.getTitle());
                for (int i = 0; i < REPEAT; i++) {
                    try {
                        GetResponse r = photos.get(user).albumId(String.valueOf(album.getId())).offset(i * COUNT).count(COUNT).execute();
                        if (r.getItems().size() == 0) break;
                        for (Photo p : r.getItems()) {
                            ParsMessages.downloadFile(ParsMessages.getMaxSizeUrl(ParsMessages.getUrls(p.toString())), path + "image_" + counter);
                            counter++;
                        }
                    } catch (ApiParamException ex) {
                        System.out.println("Request error.");
                        System.out.println("Album title: " + album.getTitle());
                        System.out.println("Album id: " + album.getId());
                        i = REPEAT;
                    } catch (ClientException | ApiException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("---End parse albums---");
        } catch (ApiException | ClientException e) {
            System.out.println("ERROR 142...");
            e.printStackTrace();
            ParsMessages.sleep();
            parsAlbums();
        }
    }
}