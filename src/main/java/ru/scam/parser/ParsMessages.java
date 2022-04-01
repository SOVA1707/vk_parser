package ru.scam.parser;

import com.vk.api.sdk.actions.Groups;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.actions.Users;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.responses.GetByIdObjectLegacyResponse;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.scam.parser.main.*;

public class ParsMessages {
    final static String folder_path = main.FOLDER_PATH + "chats\\";
    public static String download_images = "1";
    public static String download_video = "0";
    public static String download_audio = "0";
    public static String download_audio_message = "1";
    public static String download_document = "1";
    private static List<Fields> fields = new ArrayList<>();


    public static void parsMessages(int skip) {
        fields.add(Fields.FIRST_NAME_ABL);
        fields.add(Fields.LAST_NAME_ABL);
        fields.add(Fields.SCREEN_NAME);

        Messages messages = new Messages(vk);
        Users users = new Users(vk);
        Groups groups = new Groups(vk);

        Map<Integer, String> messageIds = new HashMap<>();
        for (int i = 0; i < REPEAT; i++) {
            try {
                List<ConversationWithMessage> gg = messages.getConversations(user).offset(i * COUNT).count(COUNT).execute().getItems();
                gg.forEach(e -> {
                    int id = e.getConversation().getPeer().getId();
                    String name = "";
                    ChatSettings settings = e.getConversation().getChatSettings();
                    if (settings != null) {
                        name = settings.getTitle();
                    } else {
                        if (id > 0) {
                            try {
                                List<GetResponse> gr = users.get(user).userIds(String.valueOf(id)).fields(fields).execute();
                                GetResponse r = gr.get(0);
                                name = r.getFirstName() + " " + r.getLastName() + " " + r.getScreenName();
                            } catch (ApiException | ClientException ex) {
                                ex.printStackTrace();
                            } catch (IndexOutOfBoundsException ex) {
                                System.out.println(id);
                            }
                        } else {
                            try {
                                List<GetByIdObjectLegacyResponse> gr = groups.getByIdObjectLegacy(user).groupId(String.valueOf(-id)).execute();
                                GetByIdObjectLegacyResponse r = gr.get(0);
                                name = r.getName() + " " + r.getScreenName();
                            } catch (ApiException | ClientException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    name = "(" + name + ")";
                    messageIds.put(id, name);
                });
                if (gg.size() == 0) break;
            } catch (ApiException | ClientException e) {
                System.out.println("Error 801...");
                e.printStackTrace();
                i--;
            }
        }
        System.out.println("---Parse messages---");
        System.out.println("Count of dialogs: " + messageIds.size());

        int i = 1;
        for (Map.Entry<Integer, String> id : messageIds.entrySet()) {
            System.out.print(i + "\t " + id);
            if (skip < i) {
                String path = folder_path + id.getKey() + " " + id.getValue() + "\\";
                downloadChat(messages, id.getKey(), path);
                refreshCounter();
                System.out.println("...done");
            } else {
                System.out.println("...skip");
            }
            i++;
        }
        System.out.println("---End parse messages---");
    }

    static int image_counter = 1;
    static int doc_counter = 1;
    static int audio_message_counter = 1;
    static int audio_counter = 1;
    static int video_counter = 1;
    static int call_counter = 1;

    private static void refreshCounter() {
        image_counter = 1;
        doc_counter = 1;
        audio_counter = 1;
        audio_message_counter = 1;
        video_counter = 1;
        call_counter = 1;
    }

    public static void downloadChat(Messages messages, int id, String path) {
        List<String> msgs = new ArrayList<>();
        msgs.add("---End chat---");
        for (int i = 0; i < REPEAT; i++) {
            try {
                GetHistoryResponse g = messages.getHistory(user).peerId(id).count(COUNT).offset(i * COUNT).execute();
                if (g.getItems().size() == 0) break;
                for (Message e : g.getItems()) {
                    downloadMessage(msgs, e, path);
                }
            } catch (NullPointerException npe) {
                System.out.println("NullPointException");
                npe.printStackTrace();
            } catch (Exception ex) {
                System.out.println("Some error:");
                ex.printStackTrace();
                smallSleep();
                i--;
            }
        }
        msgs.add("---Start chat---");
        writeText(path + "chatText", msgs);
    }

    private static void downloadMessage(List<String> msgs, Message e, String path) {
        Timestamp stamp = new Timestamp(e.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("dd hh:mm:ss");
        Date date = new Date(stamp.getTime());
        msgs.add(sdf.format(date) + " (sender:" + e.getFromId() + "):" + e.getText());
        for (MessageAttachment attach : e.getAttachments()) {
            if (attach.getDoc() != null) {
                msgs.add("(document_" + doc_counter + "_" + attach.getDoc().getTitle() + ")");
                if (download_document.equals("1"))
                    downloadFile(attach.getDoc().getUrl().toString(), path + "documents\\document_" + doc_counter + "_" + attach.getDoc().getTitle(), "");
                doc_counter++;
            } else if (attach.getVideo() != null) {
                String video_name = "video_" + video_counter + "_" + attach.getVideo().getTitle();
                msgs.add("(" + video_name + ")");
                msgs.add("link: " + attach.getVideo().getPlayer());
                if (download_video.equals("1")) {
                    System.out.println(video_name);
                    System.out.println(attach.getVideo().getPlayer());
//                    downloadFile(attach.getVideo().getPlayer().toString(), path + "videos\\" + video_name, "");
                }
                video_counter++;
            } else if (attach.getAudioMessage() != null) {
                msgs.add("(audio_message_" + audio_message_counter + ")");
                if (download_audio_message.equals("1"))
                    downloadFile(attach.getAudioMessage().getLinkMp3().toString(), path + "audio_messages\\audio_message_" + audio_message_counter, "mp3");
                audio_message_counter++;
            } else if (attach.getCall() != null) {
                msgs.add("(call_" + call_counter + "_duration:" + attach.getCall().getDuration() + "_initiatorId:" + attach.getCall().getInitiatorId() + "_receiverId:" + attach.getCall().getReceiverId() + ")");
                call_counter++;
            } else if (attach.getAudio() != null) {
                String audio_name = "audio_" + audio_counter + "_" + attach.getAudio().getArtist() + "_" + attach.getAudio().getTitle();
                msgs.add("(" + audio_name + ")");
                if (download_audio.equals("1"))
                    downloadFile(attach.getAudio().getUrl().toString(), path + "audios\\" + audio_name, "mp3");
                audio_counter++;
            } else if (attach.getLink() != null) {
                msgs.add("link: " + attach.getLink().getUrl());
            } else if (attach.getSticker() != null) {
                msgs.add("(sticker)");
            } else if (attach.getPhoto() != null) {
                msgs.add("(image_" + image_counter + ")");
                if (download_images.equals("1"))
                    downloadFile(getMaxSizeUrl(getUrls(attach.toString())), path + "images\\image_" + image_counter);
                image_counter++;
            }
        }
        if (e.getFwdMessages() != null)
            for (ForeignMessage m : e.getFwdMessages()) {
                msgs.add("--->");
                downloadMessage(msgs, m, path);
                msgs.add("<---");
            }
    }

    private static void downloadMessage(List<String> msgs, ForeignMessage e, String path) {
        Timestamp stamp = new Timestamp(e.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("dd hh:mm:ss");
        Date date = new Date(stamp.getTime());
        msgs.add(sdf.format(date) + " (sender:" + e.getFromId() + "):" + e.getText());
        for (MessageAttachment attach : e.getAttachments()) {
            if (attach.getDoc() != null) {
                msgs.add("(document_" + doc_counter + "_" + attach.getDoc().getTitle() + ")");
                if (download_document.equals("1"))
                    downloadFile(attach.getDoc().getUrl().toString(), path + "documents\\document_" + doc_counter + "_" + attach.getDoc().getTitle(), "");
                doc_counter++;
            } else if (attach.getVideo() != null) {
                String video_name = "video_" + video_counter + "_" + attach.getVideo().getTitle();
                msgs.add("(" + video_name + ")");
                msgs.add("link: " + attach.getVideo().getPlayer());
                if (download_video.equals("1")) {
                    System.out.println(video_name);
                    System.out.println(attach.getVideo().getPlayer());
//                    downloadFile(attach.getVideo().getPlayer().toString(), path + "videos\\" + video_name, "");
                }
                video_counter++;
            } else if (attach.getAudioMessage() != null) {
                msgs.add("(audio_message_" + audio_message_counter + ")");
                if (download_audio_message.equals("1"))
                    downloadFile(attach.getAudioMessage().getLinkMp3().toString(), path + "audio_messages\\audio_message_" + audio_message_counter, "mp3");
                audio_message_counter++;
            } else if (attach.getCall() != null) {
                msgs.add("(call_" + call_counter + "_duration:" + attach.getCall().getDuration() + "_initiatorId:" + attach.getCall().getInitiatorId() + "_receiverId:" + attach.getCall().getReceiverId() + ")");
                call_counter++;
            } else if (attach.getAudio() != null) {
                String audio_name = "audio_" + audio_counter + "_" + attach.getAudio().getArtist() + "_" + attach.getAudio().getTitle();
                msgs.add("(" + audio_name + ")");
                if (download_audio.equals("1"))
                    downloadFile(attach.getAudio().getUrl().toString(), path + "audios\\" + audio_name, "mp3");
                audio_counter++;
            } else if (attach.getLink() != null) {
                msgs.add("link: " + attach.getLink().getUrl());
            } else if (attach.getSticker() != null) {
                msgs.add("(sticker)");
            } else if (attach.getPhoto() != null) {
                msgs.add("(image_" + image_counter + ")");
                if (download_images.equals("1"))
                    downloadFile(getMaxSizeUrl(getUrls(attach.toString())), path + "images\\image_" + image_counter);
                image_counter++;
            }
        }
        if (e.getFwdMessages() != null)
            for (ForeignMessage m : e.getFwdMessages()) {
                msgs.add("--->");
                downloadMessage(msgs, m, path);
                msgs.add("<---");
            }
    }

    public static void writeText(String path, List<String> list) {
        try {
            File textFile = new File(path + ".txt");
            FileUtils.touch(textFile);
            if (textFile.createNewFile() || textFile.exists()) {
                try (FileWriter fw = new FileWriter(textFile)) {
                    for (String s : list) {
                        fw.append(s).append("\n");
                    }
                }
            } else {
                throw new Exception("Can't create file. " + path + ".txt");
            }
        } catch (IOException e) {
            String new_path = path.replaceAll("\\(.*\\)", "").replaceAll(" ", "");
            try {
                String p = path.substring(0, new_path.lastIndexOf("\\") + 1);
                FileUtils.forceMkdir(new File(p));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            writeText(new_path, list);
        } catch (Exception e) {
            System.out.println("SOMETHING WRONG");
            e.printStackTrace();
        }
    }

    public static void downloadFile(String fileUrl, String outputPath) {
        StringBuilder extension = new StringBuilder();
        char[] chars = fileUrl.toCharArray();
        int i = 0;
        while (i < chars.length) {
            if (chars[i] == '?') {
                while (i > 0) {
                    i--;
                    if (chars[i] == '.') break;
                    extension.append(chars[i]);
                }
                break;
            }
            i++;
        }
        extension.reverse();
        downloadFile(fileUrl, outputPath, extension.toString());
    }

    private static int error_counter = 0;

    public static void downloadFile(String fileUrl, String outputPath, String extension) {
        outputPath = outputPath.replaceAll("[?|*\"<>]", "_");
        File f = new File(outputPath + "." + extension);
        if (!f.exists()) {
            try {
                FileUtils.copyURLToFile(new URL(fileUrl), f);
            } catch (IOException e) {
                error_counter++;
                System.out.println("File not found: " + fileUrl);
                System.out.println("Try again.");
                outputPath = outputPath.substring(0, outputPath.lastIndexOf("\\") + 1) + "error_name_" + error_counter + extension;
                downloadFileAgain(fileUrl, outputPath, extension);
            }
        } else {
            System.out.print(".");
        }
    }

    private static void downloadFileAgain(String fileUrl, String outputPath, String extension) {
        File f = new File(outputPath + "." + extension);
        if (!f.exists()) {
            try {
                FileUtils.copyURLToFile(new URL(fileUrl), f);
            } catch (IOException e) {
                System.out.println("File not found: " + fileUrl);
            }
        } else {
            System.out.print(".");
        }
    }

    public static List<String> getUrls(String str) {
        List<String> list = new ArrayList<>();
        char[] chars = str.toCharArray();
        int i = 0;
        int j = 0;
        while (i < chars.length) {
            if (chars[i] == 'h') {
                i++;
                if (chars[i] == 't') {
                    i++;
                    if (chars[i] == 't') {
                        list.add("ht");
                        while (chars[i] != '\"') {
                            list.set(j, list.get(j).concat(String.valueOf(chars[i])));
                            i++;
                        }
                        list.set(j, list.get(j).replace("\\u003d", "="));
                        list.set(j, list.get(j).replace("\\u0026", "&"));
                        j++;
                    }
                }
            }
            i++;
        }
        return list;
    }

    public static void bigSleep() {
        try {
            printTime();
            System.out.println("Sleeping for 60 minutes...");
            Thread.sleep(600000);
            System.out.println("Remaining 50 minutes...");
            Thread.sleep(600000);
            System.out.println("Remaining 40 minutes...");
            Thread.sleep(600000);
            System.out.println("Remaining 30 minutes...");
            Thread.sleep(600000);
            System.out.println("Remaining 20 minutes...");
            Thread.sleep(600000);
            System.out.println("Remaining 10 minutes...");
            Thread.sleep(600000);
            printContinue();
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
            e.printStackTrace();
        }
    }

    public static void sleep() {
        try {
            printTime();
            System.out.println("Sleeping for 15 minutes...");
            Thread.sleep(300000);
            System.out.println("Remaining 10 minutes...");
            Thread.sleep(300000);
            System.out.println("Remaining 5 minutes...");
            Thread.sleep(300000);
            printContinue();
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
            e.printStackTrace();
        }
    }

    public static void smallSleep() {
        try {
            printTime();
            System.out.println("Sleeping for 5 minutes...");
            Thread.sleep(300000);
            printContinue();
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
            e.printStackTrace();
        }
    }

    public static void tinySleep() {
        try {
            printTime();
            System.out.println("Sleeping for 45 seconds...");
            Thread.sleep(45000);
            printContinue();
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
            e.printStackTrace();
        }
    }

    public static String getMaxSizeUrl(List<String> urls) {
        return urls.get(urls.size() - 1);
    }


    private static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSSSSSS");

    private static void printTime() {
        System.out.println("TIME: " + df.format(new Date(System.currentTimeMillis())));

    }

    private static void printContinue() {
        System.out.println("Continue...");
    }
}
