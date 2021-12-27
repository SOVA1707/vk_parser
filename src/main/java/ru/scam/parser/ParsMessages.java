package ru.scam.parser;

import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.responses.GetHistoryAttachmentsResponse;
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
    public static String token = main.token;
    public static int userId = main.userId;
    public static UserActor user = main.user;
    final static int count = main.count;
    final static int repeat = main.repeat;
    final static String folder_path = main.folder_path;

    public static void parsMessages(VkApiClient vk, UserActor user, int skip) throws Exception {

        Messages messages = new Messages(vk);

        Set<Integer> messageIds = new HashSet<>();
        for (int i = 0; i < repeat; i++) {
            List<ConversationWithMessage> gg = messages.getConversations(user).offset(i * count).count(count).execute().getItems();
            gg.forEach(e -> messageIds.add(e.getConversation().getPeer().getId()));
            if (gg.size() == 0) break;
        }
        System.out.println("Count of dialogs: " + messageIds.size());

        int i = 1;
        for (int id : messageIds) {
            System.out.println(i + ":" + id);
            if (skip < i) {
                String path = folder_path + id + "\\";
                downloadChat(messages, id, path);
                refreshCounter();
            }else {
                System.out.println("...skip");
            }
            i++;
        }
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

    private static void downloadChat(Messages messages, int id, String path) throws Exception {
        List<String> msgs = new ArrayList<>();
        msgs.add("---Конец чата---");
        for (int i = 0; i < repeat; i++) {
            try {
                GetHistoryResponse g = messages.getHistory(user).peerId(id).count(count).offset(i * count).execute();
                if (g.getItems().size() == 0) break;
                for (Message e : g.getItems()) {
                    downloadMessage(msgs, e, path);
                }
            } catch (Exception ex) {
                System.out.println("Some error:");
                ex.printStackTrace();
                System.out.println("Sleeping for 15 minutes...");
                Thread.sleep(300000);
                System.out.println("Remaining 10 minutes...");
                Thread.sleep(300000);
                System.out.println("Remaining 5 minutes...");
                Thread.sleep(300000);
            }
        }
        msgs.add("---Начало чата---");
        try {
            File textFile = new File(path + "chatText.txt");
            FileUtils.touch(textFile);
            if (textFile.createNewFile() || textFile.exists()) {
                try (FileWriter fw = new FileWriter(textFile)) {
                    for (String text : msgs) {
                        fw.append(text).append("\n");
                    }
                }
            } else {
                throw new Exception("File cannot create. " + path + "chatText.txt");
            }
        } catch (Exception e) {
            System.out.println("SOMETHING WRONG");
            e.printStackTrace();
        }
    }

    private static void downloadMessage(List<String> msgs, Message e, String path) {
        Timestamp stamp = new Timestamp(e.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("dd hh:mm:ss");
        Date date = new Date(stamp.getTime());
        msgs.add(sdf.format(date) + " (sender:" + e.getFromId() + "):" + e.getText());
        for (MessageAttachment attach : e.getAttachments()) {
            if (attach.getDoc() != null) {
                msgs.add("(document_" + doc_counter + "_" + attach.getDoc().getTitle() + ")");
                downloadFile(attach.getDoc().getUrl().toString(), path + "documents\\document_" + doc_counter + "_" + attach.getDoc().getTitle(), attach.getDoc().getExt());
                doc_counter++;
            } else if (attach.getVideo() != null) {
                String video_name = "video_" + video_counter + "_" + attach.getVideo().getTitle();
                msgs.add("(" + video_name + ")");
                msgs.add("link: " + attach.getVideo().getPlayer());
            } else if (attach.getAudioMessage() != null) {
                msgs.add("(audio_message_" + audio_message_counter + ")");
                downloadFile(attach.getAudioMessage().getLinkMp3().toString(), path + "audio_messages\\audio_message_" + audio_message_counter, "mp3");
                audio_message_counter++;
            } else if (attach.getCall() != null) {
                msgs.add("(call_" + call_counter + "_duration:" + attach.getCall().getDuration() + "_initiatorId:" + attach.getCall().getInitiatorId() + "_receiverId:" + attach.getCall().getReceiverId() + ")");
                call_counter++;
            } else if (attach.getAudio() != null) {
                String audio_name = "audio_" + audio_counter + "_" + attach.getAudio().getArtist() + "_" + attach.getAudio().getTitle();
                msgs.add("(" + audio_name + ")");
//                downloadFile(attach.getAudio().getUrl().toString(), path + "audios\\" + audio_name, "mp3");
                audio_counter++;
            } else if (attach.getLink() != null) {
                msgs.add("link: " + attach.getLink().getUrl());
            } else if (attach.getSticker() != null) {
                msgs.add("(sticker)");
            } else if (attach.getPhoto() != null) {
                msgs.add("(image_" + image_counter + ")");
                try {
                    downloadFile(getMaxSizeUrl(getUrls(attach.toString())), path + "images\\image_" + image_counter);
                } catch (Exception ex) {
                    System.out.println("-----------------");
                    System.out.println(e);
                    System.out.println("-----------------");
                    ex.printStackTrace();
                }
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
                downloadFile(attach.getDoc().getUrl().toString(), path + "documents\\document_" + doc_counter + "_" + attach.getDoc().getTitle(), attach.getDoc().getExt());
                doc_counter++;
            } else if (attach.getVideo() != null) {
                String video_name = "video_" + video_counter + "_" + attach.getVideo().getTitle();
                msgs.add("(" + video_name + ")");
                msgs.add("link: " + attach.getVideo().getPlayer());
            } else if (attach.getAudioMessage() != null) {
                msgs.add("(audio_message_" + audio_message_counter + ")");
                downloadFile(attach.getAudioMessage().getLinkMp3().toString(), path + "audio_messages\\audio_message_" + audio_message_counter, "mp3");
                audio_message_counter++;
            } else if (attach.getCall() != null) {
                msgs.add("(call_" + call_counter + "_duration:" + attach.getCall().getDuration() + "_initiatorId:" + attach.getCall().getInitiatorId() + "_receiverId:" + attach.getCall().getReceiverId() + ")");
                call_counter++;
            } else if (attach.getAudio() != null) {
                String audio_name = "audio_" + audio_counter + "_" + attach.getAudio().getArtist() + "_" + attach.getAudio().getTitle();
                msgs.add("(" + audio_name + ")");
                downloadFile(attach.getAudio().getUrl().toString(), path + "audios\\" + audio_name, "mp3");
                audio_counter++;
            } else if (attach.getLink() != null) {
                msgs.add("link: " + attach.getLink().getUrl());
            } else if (attach.getSticker() != null) {
                msgs.add("(sticker)");
            } else if (attach.getPhoto() != null) {
                msgs.add("(image_" + image_counter + ")");
                try {
                    downloadFile(getMaxSizeUrl(getUrls(attach.toString())), path + "images\\image_" + image_counter);
                } catch (Exception ex) {
                    System.out.println("-----------------");
                    System.out.println(e);
                    System.out.println("-----------------");
                    ex.printStackTrace();
                }
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

    private static void downloadImages(Messages messages, int id, String path) throws Exception {
        GetHistoryAttachmentsResponse ga = messages.getHistoryAttachments(user, id).count(count).mediaType(GetHistoryAttachmentsMediaType.PHOTO).execute();

        Map<String, String> urls = new HashMap<>();

        for (int i = 0; i < repeat; i++) {
            ga.getItems().forEach(e -> {
                String url = null;
                try {
                    url = getMaxSizeUrl(getUrls(e.toString()));
                } catch (Exception ex) {
                    System.out.println("-----------------");
                    System.out.println(e);
                    System.out.println("-----------------");
                    ex.printStackTrace();
                }
                urls.put(url.substring(url.indexOf("com") + 3), url.substring(url.indexOf("https://"), url.indexOf("com") + 3));
            });
            ga = messages.getHistoryAttachments(user, id).count(count).mediaType(GetHistoryAttachmentsMediaType.PHOTO).startFrom(ga.getNextFrom()).execute();
        }

        int counter = 0;
        System.out.println(urls.size());
        for (Map.Entry<String, String> url : urls.entrySet()) {
            counter++;
            downloadFile(url.getValue() + url.getKey(), path + "image_" + counter);
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
                System.out.println("File not found. Error:");
                e.printStackTrace();
                System.out.println("");
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

    public static String getMaxSizeUrl(List<String> urls) throws Exception {
        int index = 0;
        int multiply = 0;
        for (int i = 0; i < urls.size(); i++) {
            int begin = urls.get(i).indexOf("size=") + 5;
            int end = urls.get(i).indexOf("size=") + 5;
            while (urls.get(i).charAt(end) != 'x') {
                end++;
            }
            end++;
            boolean flag = true;
            while (flag) {
                switch (urls.get(i).charAt(end)) {
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '0':
                        break;
                    default:
                        flag = false;
                }
                end++;
            }
            String temp = urls.get(i).substring(begin, end - 1);
            String[] numbers = temp.split("x");
            int m = Integer.parseInt(numbers[0]) * Integer.parseInt(numbers[1]);
            if (m > multiply) {
                multiply = m;
                index = i;
            }
        }
        return urls.get(index);
    }
}
