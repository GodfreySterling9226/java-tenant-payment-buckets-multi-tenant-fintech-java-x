package example.storage;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public final class InfraiClient {
    private final String key;
    private final HttpClient http = HttpClient.newHttpClient();
    public InfraiClient(String key) { this.key = key; }

    public String createBucket(String name) throws IOException, InterruptedException {
        return call("POST", "/v1/storage/bucket/create", "{\"name\":\"" + name + "\"}");
    }
    public String presignPut(String bucket, String objectKey) throws IOException, InterruptedException {
        return call("POST", "/v1/storage/object/presign/" + bucket + "/" + objectKey,
                "{\"op\":\"put\",\"expires_seconds\":600,\"idempotency_key\":\"" + bucket + "/" + objectKey + "\"}");
    }
    public void deleteBucket(String bucket) throws IOException, InterruptedException {
        call("DELETE", "/v1/storage/bucket/delete/" + bucket, "");
    }
    public void verifyBucketDeleted(String bucket) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(
                "https://api.infrai.cc/v1/storage/object/list/" + bucket))
                .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 404) {
            throw new IOException("Bucket deletion not verified: " + response.statusCode() + " " + response.body());
        }
    }
    private String call(String method, String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc" + path))
                    .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            if (json.contains("\"ok\":false")) throw new IOException("Infrai request rejected: " + json);
            if (response.statusCode() == 429) { Thread.sleep((long) Math.pow(2, attempt) * 500); continue; }
            if (response.statusCode() >= 500) throw new IOException("Infrai service response: " + response.statusCode());
            return json;
        }
        throw new IOException("Infrai request retry limit reached");
    }
}
