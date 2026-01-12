package com.wuin.wi_mega.common.util;

import com.wuin.wi_mega.common.exception.APIRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * 生产环境HTTP客户端工具类
 * 使用Java 21的HttpClient API，支持同步和异步请求
 */
@Slf4j
public class HttpClientUtils {

    // 默认超时时间
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    // 单例HttpClient实例
    private static final HttpClient httpClient = createHttpClient();

    /**
     * 创建配置好的HttpClient实例
     */
    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * 发送GET请求
     *
     * @param url 目标URL
     * @return HTTP响应
     * @throws IOException          网络异常
     * @throws InterruptedException 中断异常
     */
    public static String get(String url) throws IOException, InterruptedException {
        return get(url, null);
    }

    /**
     * 发送带请求头的GET请求
     *
     * @param url     目标URL
     * @param headers 请求头
     * @return HTTP响应
     * @throws IOException          网络异常
     * @throws InterruptedException 中断异常
     */
    public static String get(String url, Map<String, String> headers)
            throws IOException, InterruptedException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(DEFAULT_TIMEOUT)
                .GET();

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return getBodyOrThrow(url, "", response);
    }

    /**
     * 发送POST请求
     *
     * @param url  目标URL
     * @param body 请求体
     * @return HTTP响应
     * @throws IOException          网络异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, String body) throws IOException, InterruptedException {
        return post(url, body, null);
    }

    /**
     * 发送带请求头的POST请求
     *
     * @param url     目标URL
     * @param body    请求体
     * @param headers 请求头
     * @return HTTP响应
     * @throws IOException          网络异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, String body, Map<String, String> headers)
            throws IOException, InterruptedException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(DEFAULT_TIMEOUT)
                .POST(null == body ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        if (headers == null || !headers.containsKey("Content-Type")) {
            requestBuilder.header("Content-Type", "application/json");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return getBodyOrThrow(url, body, response);
    }

    public static String postForm(String url, String body, Map<String, String> headers)
            throws IOException, InterruptedException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(DEFAULT_TIMEOUT)
                .POST(null == body ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }
        if (headers == null || !headers.containsKey("Content-Type")) {
            requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
        }
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return getBodyOrThrow(url, body, response);
    }


    public static String put(String url, String body, Map<String, String> headers)
            throws IOException, InterruptedException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(DEFAULT_TIMEOUT)
                .PUT(null == body ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        if (headers == null || !headers.containsKey("Content-Type")) {
            requestBuilder.header("Content-Type", "application/json");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return getBodyOrThrow(url, body, response);
    }

    public static String delete(String url, Map<String, String> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(DEFAULT_TIMEOUT)
                .DELETE();
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }
        if (headers == null || !headers.containsKey("Content-Type")) {
            requestBuilder.header("Content-Type", "application/json");
        }
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return getBodyOrThrow(url, null, response);
    }

    /**
     * 检查HTTP响应是否成功
     *
     * @param response HTTP响应
     * @return 是否成功
     */
    public static boolean isSuccess(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    /**
     * 获取响应体，如果失败则抛出异常
     *
     * @param response HTTP响应
     * @return 响应体
     * @throws IOException 如果响应不成功
     */
    public static String getBodyOrThrow(String url, String body, HttpResponse<String> response) {
        if (!isSuccess(response)) {
            log.error("http post -> return status is not 2xx, url:{}, body:{}, statusCode:{}", url, null == body ? "null" : body, response.statusCode());
            throw new APIRuntimeException(response.statusCode(), response.body());
        }
        return response.body();
    }
} 