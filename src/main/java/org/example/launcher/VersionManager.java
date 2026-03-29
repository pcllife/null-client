package org.example.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.utils.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

public class VersionManager {

    private static final String VERSIONS_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";
    private static final Path MINECRAFT_DIR = Paths.get(System.getProperty("user.home"), ".nullclient");
    private static final Path VERSIONS_DIR = MINECRAFT_DIR.resolve("versions");
    private static final Path LIBRARIES_DIR = MINECRAFT_DIR.resolve("libraries");

    public VersionManager() {
        // 创建必要目录
        try {
            Files.createDirectories(VERSIONS_DIR);
            Files.createDirectories(LIBRARIES_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取可用版本列表（简化：从官方清单获取所有 release 版本）
     */
    public List<String> getAvailableVersions() {
        List<String> versions = new ArrayList<>();
        try {
            String manifestJson = downloadString(VERSIONS_MANIFEST_URL);
            JsonObject root = new Gson().fromJson(manifestJson, JsonObject.class);
            var versionsArray = root.getAsJsonArray("versions");
            for (var element : versionsArray) {
                JsonObject obj = element.getAsJsonObject();
                String id = obj.get("id").getAsString();
                versions.add(id);
            }
            // 倒序（最新的在上）
            Collections.reverse(versions);
        } catch (Exception e) {
            e.printStackTrace();
            // 降级：返回默认版本
            versions.add("1.20.4");
        }
        return versions;
    }

    /**
     * 下载并解析指定版本的 JSON，确保所有库文件存在
     */
    public void prepareVersion(String versionId) throws IOException {
        // 1. 获取版本详情 URL
        String manifestJson = downloadString(VERSIONS_MANIFEST_URL);
        JsonObject root = new Gson().fromJson(manifestJson, JsonObject.class);
        var versionsArray = root.getAsJsonArray("versions");
        String versionUrl = null;
        for (var element : versionsArray) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.get("id").getAsString().equals(versionId)) {
                versionUrl = obj.get("url").getAsString();
                break;
            }
        }
        if (versionUrl == null) throw new IOException("未找到版本: " + versionId);

        // 2. 下载版本 JSON
        String versionJson = downloadString(versionUrl);
        JsonObject versionObj = new Gson().fromJson(versionJson, JsonObject.class);

        // 3. 下载 libraries
        var libraries = versionObj.getAsJsonArray("libraries");
        for (var libElem : libraries) {
            JsonObject libObj = libElem.getAsJsonObject();
            if (libObj.has("rules")) {
                // 简化：忽略规则，假设总是下载（实际需判断 OS）
            }
            JsonObject downloads = libObj.getAsJsonObject("downloads");
            if (downloads != null && downloads.has("artifact")) {
                JsonObject artifact = downloads.getAsJsonObject("artifact");
                String url = artifact.get("url").getAsString();
                String path = artifact.get("path").getAsString();
                downloadLibrary(url, path);
            }
            // 还有 natives 部分，这里略
        }

        // 4. 保存版本 JSON 到本地（供启动时使用）
        Path versionJsonPath = VERSIONS_DIR.resolve(versionId).resolve(versionId + ".json");
        Files.createDirectories(versionJsonPath.getParent());
        Files.writeString(versionJsonPath, versionJson);
    }

    private void downloadLibrary(String url, String relativePath) throws IOException {
        Path target = LIBRARIES_DIR.resolve(relativePath);
        if (Files.exists(target)) return;
        Files.createDirectories(target.getParent());
        downloadFile(url, target);
    }

    private String downloadString(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (InputStream is = conn.getInputStream();
             Scanner scanner = new Scanner(is, "UTF-8")) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private void downloadFile(String urlStr, Path dest) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Path getVersionJsonPath(String versionId) {
        return VERSIONS_DIR.resolve(versionId).resolve(versionId + ".json");
    }
}