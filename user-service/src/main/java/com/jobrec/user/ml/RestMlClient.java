package com.jobrec.user.ml;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

public class RestMlClient implements MlClient {
    private final RestClient client;
    public RestMlClient(String baseUrl, long connectTimeoutMs, long readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectTimeoutMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
    @Override public MlExtraction extractResume(byte[] content, String filename, String contentType) { return extract("/extract/resume", content, filename, contentType, ResumeResponse.class); }
    @Override public MlExtraction extractCertificate(byte[] content, String filename, String contentType) { return extract("/extract/certificate", content, filename, contentType, CertificateResponse.class); }
    private <T extends ExtractionResponse> MlExtraction extract(String path, byte[] content, String filename, String contentType, Class<T> responseType) {
        try {
            ByteArrayResource resource = new ByteArrayResource(content) { @Override public String getFilename() { return filename; } };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(); body.add("file", resource);
            T response = client.post().uri(path).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(responseType);
            return new MlExtraction(response == null || response.skills() == null ? List.of() : response.skills().stream().map(skill -> new MlSkill(skill.skill(), skill.confidence())).toList());
        } catch (Exception exception) { throw new MlUnavailableException("ML service is unavailable", exception); }
    }
    private interface ExtractionResponse { List<ExtractedSkill> skills(); }
    private record ExtractedSkill(String skill, double confidence) {}
    private record ResumeResponse(List<ExtractedSkill> matchedSkills) implements ExtractionResponse { public List<ExtractedSkill> skills() { return matchedSkills; } }
    private record CertificateResponse(List<ExtractedSkill> detectedSkills) implements ExtractionResponse { public List<ExtractedSkill> skills() { return detectedSkills; } }
}
