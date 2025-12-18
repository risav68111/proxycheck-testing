package proxytest.sel;

import java.time.Instant;
import java.util.UUID;

public class ProxyVar {
    private UUID id;
    private String proxy;
    private String user;
    private String pass;
    @Override
    public String toString() {
        return "ProxyVar{id=" + id + ", proxy=" + proxy + ", user=" + user + ", pass=" + pass + ", count=" + count
                + ", modifiedAt=" + modifiedAt + "}";
    }
    private int count;

    // public Proxy(Instant modifiedAt, UUID id, String proxy, String user, int count) {
    //     this.modifiedAt = modifiedAt;
    //     this.id = id;
    //     this.proxy = proxy;
    //     this.user = user;
    //     this.count = count;
    // }

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
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPass() {
        return pass;
    }
    public void setPass(String pass) {
        this.pass = pass;
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
