package com.jobrec.user.ml;

import java.util.List;

public interface MlClient {
    MlExtraction extractResume(byte[] content, String filename, String contentType);
    MlExtraction extractCertificate(byte[] content, String filename, String contentType);

    record MlSkill(String name, double confidence) {}
    record MlExtraction(List<MlSkill> skills) {}
}
