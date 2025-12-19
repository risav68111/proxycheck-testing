package proxytest.sel;

import java.time.Instant;
import java.util.UUID;

public class ProxyVar {
    private UUID id;
    private String proxy;
    private String username;
    private String password;
    private Instant createdAt;
    private int count;
    private Instant modifiedAt;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getProxy() {
        return proxy;
    }
    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public Instant getModifiedAt() {
        return modifiedAt;
    }
    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

}
