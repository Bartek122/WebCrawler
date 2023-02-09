package pl.coderslab;

import com.github.slugify.Slugify;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    private static final String URL = "https://www.infoworld.com";
    private static final String FILE_EXTENSION = ".txt";
    private static final String PATH = "Files";
    public static void main(String[] args) {

        final Slugify slugify = Slugify.builder().build();
        Element elm = null;
        try {
            elm = Jsoup.connect(URL.concat("/category/java/")).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> list;
        list = elm.select("h3").stream()
                .map(obj -> Objects.toString(obj, null))
                .collect(Collectors.toList());

        list = list.stream()
                .map(s -> s.replace("<h3><a href=\"", ""))
                .map(s -> s.replace("</a></h3>", ""))
                .collect(Collectors.toList());
        Map<String, String> mapStr = new HashMap<>();
        for (String s : list) {
            String uuid = UUID.randomUUID().toString();
            int i = s.lastIndexOf("\">");
            String result = slugify.slugify(uuid + s.substring(i + 2));
            mapStr.put(s.substring(0, i), result);
        }
        deleteFolderIfExist();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        mapStr.forEach((url,filename) -> executorService.execute(() -> dataFromArticle(url, filename)));
        executorService.shutdown();
    }

    private static void dataFromArticle(String url, String filename) {
        try {
            Document document = Jsoup.connect(URL.concat(url)).get();
            Elements element = document.select("div[id=drr-container]");
            saveContents(filename + FILE_EXTENSION, element.text());
            System.out.println(Thread.currentThread().getName() + " " + filename + FILE_EXTENSION);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void deleteFolderIfExist() {
        File dir = new File(PATH);
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static File saveContents(String fileName, String contents) throws IOException {
        File createFolder = new File(PATH);
        File createFile = new File(PATH, fileName);
        if (!createFolder.exists()) {
            FileUtils.createParentDirectories(createFolder);
        }
        FileUtils.writeStringToFile(createFile, contents, UTF_8);
        return createFile;
    }
}