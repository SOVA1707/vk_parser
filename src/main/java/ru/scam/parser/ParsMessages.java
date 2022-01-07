package ru.scam.parser;

import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParsMessages {
    final static VkApiClient vk = main.vk;
    final static UserActor user = main.user;
    final static int count = main.count;
    final static int repeat = main.repeat;
    final static String folder_path = main.folder_path + "chats\\";
    public static String download_images = "1";
    public static String download_video = "0";
    public static String download_audio = "0";
    public static String download_audio_message = "1";
    public static String download_document = "1";


    public static void parsMessages(int skip){

        Messages messages = new Messages(vk);

        Set<Integer> messageIds = new HashSet<>();
        for (int i = 0; i < repeat; i++) {
            try {
                List<ConversationWithMessage> gg = messages.getConversations(user).offset(i * count).count(count).execute().getItems();
                gg.forEach(e -> messageIds.add(e.getConversation().getPeer().getId()));
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
        for (int id : messageIds) {
            System.out.print(i + "\t " + id);
            if (skip < i) {
                String path = folder_path + id + "\\";
                downloadChat(messages, id, path);
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

    public static void downloadChat(Messages messages, int id, String path){
        List<String> msgs = new ArrayList<>();
        msgs.add("---End chat---");
        for (int i = 0; i < repeat; i++) {
            try {
                GetHistoryResponse g = messages.getHistory(user).peerId(id).count(count).offset(i * count).execute();
                if (g.getItems().size() == 0) break;
                for (Message e : g.getItems()) {
                    downloadMessage(msgs, e, path);
                }
            } catch (NullPointerException npe) {
                System.out.println("NullPointException");
                npe.printStackTrace();
            }catch (Exception ex) {
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

    public static void downloadFile(String fileUrl, String outputPath, String extension) {
        outputPath = outputPath.replaceAll("[?]", "_");
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

    public static void sleep() {
        try {
            System.out.println("Sleeping for 15 minutes...");
            Thread.sleep(300000);
            System.out.println("Remaining 10 minutes...");
            Thread.sleep(300000);
            System.out.println("Remaining 5 minutes...");
            Thread.sleep(300000);
            System.out.println("Repeat...");
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
            e.printStackTrace();
        }
    }

    public static void smallSleep() {
        try {
            System.out.println("Sleeping for 5 minutes...");
            Thread.sleep(300000);
            System.out.println("Repeat...");
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
            e.printStackTrace();
        }
    }

    public static String getMaxSizeUrl(List<String> urls) {
        return urls.get(urls.size() - 1);
    }
}
