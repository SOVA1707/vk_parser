package ru.scam.parser;

import com.vk.api.sdk.actions.Docs;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.docs.Doc;

public class ParsDocs {
    final static VkApiClient vk = main.vk;
    final static UserActor user = main.user;
    final static String folder_path = main.folder_path + "docs\\";

    public static void parsDocs(){
        Docs docs = new Docs(vk);
        System.out.println("---Parse docs---");
        try {
            for (Doc doc : docs.get(user).execute().getItems()) {
                System.out.println(doc.getTitle());
                ParsMessages.downloadFile(doc.getUrl().toString(), folder_path + doc.getTitle(), "");
            }
        } catch (ApiException | ClientException e) {
            System.out.println("Error 1982...");
            e.printStackTrace();
            ParsMessages.sleep();
            parsDocs();
        }
        System.out.println("---End parse docs---");
    }
}
