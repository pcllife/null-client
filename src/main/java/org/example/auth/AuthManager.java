package org.example.auth;

import java.util.UUID;

public class AuthManager {
    private String username = "Player";
    private String uuid = UUID.randomUUID().toString().replace("-", "");
    private String accessToken = "null";

    public void setOfflineUsername(String name) {
        this.username = name;
        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()).toString().replace("-", "");
    }

    public String getUsername() {
        return username;
    }

    public String getUUID() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }
}