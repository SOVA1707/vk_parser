package ru.scam.parser;

import com.vk.api.sdk.actions.Docs;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.docs.Doc;

public class ParsDocs {
    final static String folder_path = main.folder_path + "docs\\";

    public static void parsDocs(VkApiClient vk, UserActor user) throws Exception {
        Docs docs = new Docs(vk);
        for (Doc doc : docs.get(user).execute().getItems()) {
            System.out.println(doc.getTitle());
            ParsMessages.downloadFile(doc.getUrl().toString(), folder_path + doc.getTitle(), "");
        }
    }
}
