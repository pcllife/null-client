package org.example.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.auth.AuthManager;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameLauncher {

    private final String versionId;
    private final AuthManager authManager;
    private final VersionManager versionManager;

    public GameLauncher(String versionId, AuthManager authManager) {
        this.versionId = versionId;
        this.authManager = authManager;
        this.versionManager = new VersionManager();
    }

    public void launch() throws Exception {
        // 1. 准备版本（下载缺失文件）
        versionManager.prepareVersion(versionId);

        // 2. 读取版本 JSON
        Path versionJsonPath = versionManager.getVersionJsonPath(versionId);
        String jsonStr = Files.readString(versionJsonPath);
        JsonObject versionObj = new Gson().fromJson(jsonStr, JsonObject.class);

        // 3. 构建 classpath
        List<Path> classpath = new ArrayList<>();

        // 添加 libraries
        JsonArray libraries = versionObj.getAsJsonArray("libraries");
        for (var libElem : libraries) {
            JsonObject libObj = libElem.getAsJsonObject();
            JsonObject downloads = libObj.getAsJsonObject("downloads");
            if (downloads != null && downloads.has("artifact")) {
                String path = downloads.getAsJsonObject("artifact").get("path").getAsString();
                Path libFile = Paths.get(System.getProperty("user.home"), ".nullclient", "libraries", path);
                if (Files.exists(libFile)) {
                    classpath.add(libFile);
                }
            }
            // natives 处理略（需要解压 natives 并加入 classpath）
        }

        // 添加主 JAR（client.jar）
        JsonObject downloads = versionObj.getAsJsonObject("downloads");
        if (downloads != null && downloads.has("client")) {
            String clientUrl = downloads.getAsJsonObject("client").get("url").getAsString();
            Path clientJar = Paths.get(System.getProperty("user.home"), ".nullclient", "versions", versionId, versionId + ".jar");
            if (!Files.exists(clientJar)) {
                versionManager.downloadFile(clientUrl, clientJar);
            }
            classpath.add(clientJar);
        }

        // 4. 构建 JVM 参数（根据版本 JSON 中的 arguments）
        List<String> jvmArgs = new ArrayList<>();
        // 设置游戏目录
        jvmArgs.add("-DgameDirectory=" + Paths.get(System.getProperty("user.home"), ".nullclient").toAbsolutePath());
        jvmArgs.add("-Djava.library.path=" + getNativesPath()); // 需要处理 natives
        // 添加默认 JVM 参数（如内存）
        jvmArgs.add("-Xmx2G");
        jvmArgs.add("-XX:+UseG1GC");

        // 5. 构建游戏参数
        List<String> gameArgs = new ArrayList<>();
        gameArgs.add("--username");
        gameArgs.add(authManager.getUsername());
        gameArgs.add("--version");
        gameArgs.add(versionId);
        gameArgs.add("--gameDir");
        gameArgs.add(Paths.get(System.getProperty("user.home"), ".nullclient").toString());
        gameArgs.add("--assetsDir");
        gameArgs.add(Paths.get(System.getProperty("user.home"), ".nullclient", "assets").toString());
        gameArgs.add("--assetIndex");
        gameArgs.add(versionObj.getAsJsonObject("assetIndex").get("id").getAsString());
        gameArgs.add("--uuid");
        gameArgs.add(authManager.getUUID());
        gameArgs.add("--accessToken");
        gameArgs.add(authManager.getAccessToken());
        gameArgs.add("--userType");
        gameArgs.add("mojang");

        // 6. 构建完整命令
        List<String> command = new ArrayList<>();
        command.add(ProcessHandle.current().info().command().orElse("java"));
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(buildClasspath(classpath));
        command.add(versionObj.get("mainClass").getAsString());
        command.addAll(gameArgs);

        // 7. 启动进程
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO(); // 将子进程输出重定向到当前控制台
        pb.directory(Paths.get(System.getProperty("user.home"), ".nullclient").toFile());
        Process process = pb.start();
        // 可选：等待游戏退出
        // int exitCode = process.waitFor();
    }

    private String buildClasspath(List<Path> paths) {
        return String.join(File.pathSeparator, paths.stream().map(Path::toString).toList());
    }

    private String getNativesPath() {
        // 返回 natives 解压后的目录，例如 ~/.nullclient/natives/1.20.4
        Path nativesDir = Paths.get(System.getProperty("user.home"), ".nullclient", "natives", versionId);
        try {
            Files.createDirectories(nativesDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nativesDir.toString();
    }
}