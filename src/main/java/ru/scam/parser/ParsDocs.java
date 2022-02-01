package ru.scam.parser;

import com.vk.api.sdk.actions.Docs;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.docs.Doc;

import static ru.scam.parser.main.*;

public class ParsDocs {
    final static String folder_path = main.FOLDER_PATH + "docs\\";

    public static void parsDocs() {
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
