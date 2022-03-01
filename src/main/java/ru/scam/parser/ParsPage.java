package ru.scam.parser;

import com.vk.api.sdk.actions.Users;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;

import java.util.ArrayList;
import java.util.List;

import static ru.scam.parser.main.*;

public class ParsPage {

    public static void parseUser(String path, String id) {
        List<Fields> fields = new ArrayList<>();
        for (Fields str : Fields.values()) {
            String s = str.name();
            fields.add(Fields.valueOf(str.name()));
        }

        parse(path, id, fields);
    }

    private static void parse(String path, String id, List<Fields> fields) {
        Users users = new Users(vk);

        try {
            List<GetResponse> g = users.get(user).userIds(id).fields(fields).execute();
            List<String> list = new ArrayList<>();
            for (GetResponse r : g) {
                String path2 = path + r.getFirstName() + " " + r.getLastName();
                list.add(r.getFirstName() + " " + r.getLastName());
                if (r.getNickname() != null) list.add("(" + r.getScreenName() + ")");
                if (r.getId() != null) list.add("Id: " + r.getId());
                if (r.getBdate() != null) list.add("Birthday: " + r.getBdate());
                if (r.getStatus() != null) list.add("Status: " + r.getStatus());
                if (r.getSex() != null) list.add("Sex: " + r.getSex().name());
                if (r.getMobilePhone() != null) list.add("Phone: " + r.getMobilePhone());
                if (r.getHomePhone() != null) list.add("Home Phone: " + r.getHomePhone());
                if (r.getCountry() != null) list.add("Country: " + r.getCountry().getTitle());
                if (r.getCity() != null) list.add("City: " + r.getCity().getTitle());
                if (r.getHomeTown() != null) list.add("Home town: " + r.getHomeTown());
                if (r.getAbout() != null) list.add("About: " + r.getAbout());
                if (r.getEmail() != null) list.add("Email: " + r.getEmail());
                if (r.getInstagram() != null) list.add("Instagram: " + r.getInstagram());
                if (r.getFacebook() != null) list.add("Facebook: " + r.getFacebook());
                if (r.getTwitter() != null) list.add("Twitter: " + r.getTwitter());
                if (r.getSkype() != null) list.add("Skype: " + r.getSkype());
                if (r.getSite() != null) list.add("Site: " + r.getSite());

                list.add("-------------\n");

                ParsMessages.writeText(path2, list);
            }

        } catch (ApiException | ClientException e) {
//            System.out.println("Error 209...");
            String text = e.getMessage();
            if (text.contains("but was")) {
                String remove = text.substring(text.indexOf("$[0].") + 5).toUpperCase();
//                System.out.println(remove);
                fields.remove(Fields.valueOf(remove));
                parse(path, id, fields);
            }
        }
    }
}
