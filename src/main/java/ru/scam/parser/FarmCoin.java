package ru.scam.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.ApiRequest;
import com.vk.api.sdk.exceptions.ApiCaptchaException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.base.Error;
import com.vk.api.sdk.objects.base.RequestParam;
import com.vk.api.sdk.objects.messages.ConversationWithMessage;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import org.apache.commons.io.FileUtils;
import ru.scam.parser.calc.Tool;

import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static ru.scam.parser.main.*;

public class FarmCoin {
    private static final Messages MESSAGES = new Messages(vk);

    public static void Farm(int level) {
        Tool.loadLibraries();

        for (int i = 0; i < REPEAT; i++) {
            try {
                List<ConversationWithMessage> gg = MESSAGES.getConversations(user).
                        offset(i * COUNT).count(COUNT).
                        execute().getItems();
                gg.forEach(e -> {
                    try {
                        int id = e.getConversation().getPeer().getId();
                        List<Message> items = MESSAGES.getHistory(user).peerId(id).count(5).execute().getItems();
                        for (Message ms : items) {
                            if (ms.getText().contains("Решите пример")) {
                                System.out.println("desired id: " + id);
                                startFarm(id, level);
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

    public static void endFarm() {
        flag = false;
    }

    private static boolean flag = true;
    private static void startFarm(int id, int level) {
        String path = FARM_PATH + "img.jpg";
        int coin_income = 0;
        int xp_income = 0;
        while(flag) {
            int SLEEP = (int) (0);
            try {
                Message message = MESSAGES.getHistory(user).peerId(id).count(1).execute().getItems().get(0);
                String t = message.getText();
                if (t.contains("Coin")) {
                    coin_income += Integer.parseInt(t.substring(t.indexOf("Вы получили ") + 12, t.indexOf(" VK Coin")));
                    xp_income += Integer.parseInt(t.substring(t.indexOf("Coin и ") + 7, t.indexOf("опыта") - 7));
                    System.out.println("~Coin income: " + coin_income + "~");
                    System.out.println("~XP income: " + xp_income + "~");
                }
                List<MessageAttachment> as = message.getAttachments();
                if (as.size() > 0) {
                    MessageAttachment ma = as.get(0);
                    if (ma.getPhoto() != null) {
                        File f = new File(path);
                        if (f.exists()) FileUtils.deleteQuietly(f);
                        ParsMessages.downloadFile(ParsMessages.getMaxSizeUrl(ParsMessages.getUrls(ma.toString())), path, "");
                        String ans;
                        if (level > 10) {
                            ans = Tool.getFractionalFromImage(path);
                        } else {
                            ans = Tool.getEquationFromImage(path);
                        }
                        if (ans.contains(".")) ans = ans.substring(0, ans.indexOf("."));
                        System.out.println("Send answer: " + ans);
                        MESSAGES.send(user).message(ans).randomId((int) System.nanoTime()).peerId(id).execute();
                        Thread.sleep(SLEEP);
                    }
                } else {
                    System.out.println("request new image");
                    MESSAGES.send(user).message("Ур. " + level).payload("{\"action\":\"level\",\"level\":" + level + "}").randomId((int) System.nanoTime()).peerId(id).execute();
                    Thread.sleep(SLEEP);
                }
            } catch (ApiException e) {
                if (e.toString().contains("Captcha needed")) {
                    try {
                        computeCaptcha(id);
                    } catch (ClientException | ApiException ex) {
                        ex.printStackTrace();
                    }
                    ParsMessages.tinySleep();
                } else {
                    ParsMessages.smallSleep();
                }
            } catch (ClientException | InterruptedException e) {
                ParsMessages.smallSleep();
            } catch (Exception e) {
                e.printStackTrace();
                ParsMessages.tinySleep();
            }
        }
    }

    public static void computeCaptcha(int id) throws ClientException, ApiException {
        System.out.println("----------");

        ApiRequest<Integer> r = MESSAGES.send(user).message(".").randomId((int) System.nanoTime()).peerId(100);

        String textResponse = r.executeAsString();
        JsonReader jsonReader = new JsonReader(new StringReader(textResponse));
        JsonObject json = (JsonObject) new JsonParser().parse(jsonReader);
        JsonObject jsonError = json.getAsJsonObject("error");

        String captcha_sid = String.valueOf(jsonError.getAsJsonPrimitive("captcha_sid"));
        String sid = captcha_sid.substring(1, captcha_sid.lastIndexOf("\""));
        String captcha_img = String.valueOf(jsonError.getAsJsonPrimitive("captcha_img"));
        String img = captcha_img.substring(1, captcha_img.lastIndexOf("\""));

        System.out.println(sid);
        System.out.println(img);

        String key = getCaptchaKey(img);

        MESSAGES.send(user).message(".").randomId((int) System.nanoTime()).peerId(id).captchaKey(key).captchaSid(sid).execute();
    }

    private static String getCaptchaKey(String img) {
        ParsMessages.downloadFile(img, FARM_PATH);
        Scanner sc = new Scanner(System.in);
        String answer = sc.nextLine();
        return answer;
    }
}
