package ru.scam.parser;

import com.vk.api.sdk.actions.Account;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.account.responses.GetProfileInfoResponse;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ParsPage {
    final static VkApiClient vk = main.vk;
    final static UserActor user = main.user;
    final static String folder_path = main.folder_path;

    public static void parsUserPage() {
        Account account = new Account(vk);
        try {
            GetProfileInfoResponse r2 = account.getProfileInfo(user).execute();

            System.out.println("---Parse user page---");

            List<String> text = new ArrayList<>();
            text.add(r2.getFirstName() + " " + r2.getLastName());
            text.add("(" + r2.getScreenName() + ")");
            text.add("День Рождения: " + r2.getBdate());
            text.add("Статус: " + r2.getStatus());
            text.add("Пол: " + r2.getSex().name());
            text.add("Телефон: " + r2.getPhone());
            text.add("Страна: " + r2.getCountry().getTitle());
            text.add("Город: " + r2.getCity().getTitle());
            text.add("Родной город: " + r2.getHomeTown());

            String path = folder_path + r2.getFirstName() + " " + r2.getLastName();

        try {
            File textFile = new File(path + ".txt");
            FileUtils.touch(textFile);
            if (textFile.createNewFile() || textFile.exists()) {
                try (FileWriter fw = new FileWriter(textFile)) {
                    for (String s : text) {
                        fw.append(s).append("\n");
                    }
                }
            } else {
                throw new Exception("Can't create file. " + path + ".txt");
            }
        } catch (Exception e) {
            System.out.println("SOMETHING WRONG");
            e.printStackTrace();
        }
            System.out.println("---End parse user page---");
        } catch (ApiException | ClientException e) {
            System.out.println("Error 14...");
            e.printStackTrace();
            ParsMessages.sleep();
        }

    }

    public static void ParsUser() {

    }
}