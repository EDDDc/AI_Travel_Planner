package com.ai.travel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class XFYunAsrClient {

    @Value("${app.asr.xfyun.appId:}")
    private String appId;

    @Value("${app.asr.xfyun.apiKey:}")
    private String apiKey;

    @Value("${app.asr.xfyun.apiSecret:}")
    private String apiSecret;

    private static final String HOST = "iat-api.xfyun.cn";
    private static final String PATH = "/v2/iat";
    private static final String URL = "wss://" + HOST + PATH;
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean isConfigured() {
        return StringUtils.hasText(appId) && StringUtils.hasText(apiKey) && StringUtils.hasText(apiSecret);
    }

    public String recognizeMp3(byte[] audioMp3) throws Exception {
        if (audioMp3 == null || audioMp3.length == 0) return "";
        String authUrl = assembleAuthUrl();

        HttpClient client = HttpClient.newHttpClient();
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<StringBuilder> result = new AtomicReference<>(new StringBuilder());
        AtomicReference<String> err = new AtomicReference<>(null);

        WebSocket ws = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .buildAsync(URI.create(authUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        WebSocket.Listener.super.onOpen(webSocket);
                        try {
                            // first frame status=0 with common/business
                            String frame0 = mapper.createObjectNode()
                                    .set("common", mapper.createObjectNode().put("app_id", appId))
                                    .toString();
                            // We'll build full JSON with business & data
                            String base64 = Base64.getEncoder().encodeToString(audioMp3);
                            JsonNode root = mapper.createObjectNode();
                            ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("common",
                                    mapper.createObjectNode().put("app_id", appId));
                            ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("business",
                                    mapper.createObjectNode()
                                            .put("domain", "iat")
                                            .put("language", "zh_cn")
                                            .put("accent", "mandarin")
                                            .put("vad_eos", 5000)
                                            .put("dwa", "wpgs"));
                            ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("data",
                                    mapper.createObjectNode()
                                            .put("status", 2) // send all in one frame and finish
                                            .put("format", "audio/L16;rate=16000")
                                            .put("encoding", "lame")
                                            .put("audio", base64));

                            webSocket.sendText(root.toString(), true);
                        } catch (Exception e) {
                            err.set(e.getMessage());
                            webSocket.abort();
                            done.countDown();
                        }
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        try {
                            JsonNode node = mapper.readTree(data.toString());
                            int code = node.path("code").asInt(0);
                            if (code != 0) {
                                err.set(node.path("message").asText("ASR error"));
                                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "error");
                                done.countDown();
                                return CompletableFuture.completedFuture(null);
                            }
                            JsonNode resultNode = node.path("data").path("result");
                            if (!resultNode.isMissingNode()) {
                                // accumulate text from ws.cw.w
                                StringBuilder sb = result.get();
                                JsonNode wsArr = resultNode.path("ws");
                                if (wsArr.isArray()) {
                                    for (JsonNode wsn : wsArr) {
                                        JsonNode cwArr = wsn.path("cw");
                                        if (cwArr.isArray()) {
                                            for (JsonNode c : cwArr) {
                                                sb.append(c.path("w").asText(""));
                                            }
                                        }
                                    }
                                }
                                result.set(sb);
                            }
                            int status = node.path("data").path("status").asInt(0);
                            if (status == 2) { // complete
                                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok");
                                done.countDown();
                            }
                        } catch (Exception ex) {
                            err.set(ex.getMessage());
                            webSocket.abort();
                            done.countDown();
                        }
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        err.set(error.getMessage());
                        done.countDown();
                    }
                }).join();

        try { done.await(); } catch (InterruptedException ignored) {}
        if (ws != null) { try { ws.abort(); } catch (Exception ignored) {} }
        if (err.get() != null) throw new RuntimeException(err.get());
        return result.get().toString();
    }

    private String assembleAuthUrl() throws Exception {
        String date = gmtDate();
        String signatureOrigin = String.format("host: %s\n", HOST) +
                String.format("date: %s\n", date) +
                String.format("GET %s HTTP/1.1", PATH);
        String signatureSha = hmacSha256Base64(signatureOrigin, apiSecret);
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                apiKey, signatureSha);
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        String url = URL + "?authorization=" + urlEncode(authorization)
                + "&date=" + urlEncode(date)
                + "&host=" + urlEncode(HOST);
        return url;
    }

    private static String gmtDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new java.util.Date());
    }

    private static String hmacSha256Base64(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(raw);
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}

