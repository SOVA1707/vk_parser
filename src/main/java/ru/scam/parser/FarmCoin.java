package ru.scam.parser;

import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.ConversationWithMessage;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import org.apache.commons.io.FileUtils;
import ru.scam.parser.calc.Tool;

import java.io.File;
import java.util.List;

import static ru.scam.parser.main.*;

public class FarmCoin {
    private static final Messages MESSAGES = new Messages(vk);

    public static void Farm() {

        for (int i = 0; i < REPEAT; i++) {
            try {
                List<ConversationWithMessage> gg = MESSAGES.getConversations(user).offset(i * COUNT).count(COUNT).execute().getItems();
                gg.forEach(e -> {
                    try {
                        int id = e.getConversation().getPeer().getId();
                        List<Message> items = MESSAGES.getHistory(user).peerId(id).count(1).execute().getItems();
                        for (Message ms : items) {
                            if (ms.getText().contains("Решите пример:")) {
                                System.out.println("desired id: " + id);
                                startFarm(id);
                                break;
                            }
                        }
                    } catch (ApiException | ClientException ex) {
                        ex.printStackTrace();
                    }
                });
                if (gg.size() == 0) break;
            } catch (ApiException | ClientException e) {
                System.out.println("Error 801...");
                e.printStackTrace();
                i--;
            }
        }
    }

    private static void startFarm(int id) {
        String path = FARM_PATH + "img.jpg";
        int SLEEP = 1000;
        int coin_income = 0;
        int xp_income = 0;
        while(true) {
            try {
                Message item = MESSAGES.getHistory(user).peerId(id).count(1).execute().getItems().get(0);
                String t = item.getText();
                if (t.contains("Coin")) {
                    coin_income += Integer.parseInt(t.substring(t.indexOf("Вы получили ") + 12, t.indexOf(" VK Coin")));
                    xp_income += Integer.parseInt(t.substring(t.indexOf("Coin и ") + 7, t.indexOf(" очка опыта")));
                    System.out.println("Coin income: " + coin_income);
                    System.out.println("XP income: " + xp_income);
                    System.out.println("request new image");
                    MESSAGES.send(user).message("Ур. 4").payload("{\"action\":\"level\",\"level\":4}").randomId((int) System.nanoTime()).peerId(id).execute();
                    Thread.sleep(SLEEP);
                }
                List<MessageAttachment> as = item.getAttachments();
                if (as.size() > 0) {
                    MessageAttachment ma = as.get(0);
                    if (ma.getPhoto() != null) {
                        File f = new File(path);
                        if (f.exists()) FileUtils.deleteQuietly(f);
                        ParsMessages.downloadFile(ParsMessages.getMaxSizeUrl(ParsMessages.getUrls(ma.toString())), path, "");
                        String ans = Tool.getEquationFromImage(path);
                        ans = ans.substring(0, ans.indexOf("."));
                        System.out.println("answer: " + ans);
                        MESSAGES.send(user).message(ans).randomId((int) System.nanoTime()).peerId(id).execute();
                        System.out.println("send answer");
                        Thread.sleep(SLEEP);
                    }
                }
            } catch (ApiException | ClientException | InterruptedException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
