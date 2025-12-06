const username = "user";      // ← your real username
const password = "proxy password";      // ← your real password
const proxyHost = "rotating.proxy.webshare.io";
const proxyPort = 80;

chrome.proxy.settings.set({
  value: {
    mode: "fixed_servers",
    rules: {
      singleProxy: {
        scheme: "http",
        host: proxyHost,
        port: proxyPort
      },
      bypassList: ["localhost", "127.0.0.1"]
    }
  },
  scope: "regular"
});

chrome.webRequest.onAuthRequired.addListener(
  () => ({
    authCredentials: { username, password }
  }),
  { urls: ["<all_urls>"] },
  ["blocking"]
);
