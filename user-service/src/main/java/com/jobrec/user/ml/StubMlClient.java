package com.jobrec.user.ml;

import java.util.List;

public class StubMlClient implements MlClient {
    private MlExtraction resume = new MlExtraction(List.of(new MlSkill("python", .95)));
    private MlExtraction certificate = new MlExtraction(List.of(new MlSkill("aws", .92)));
    private RuntimeException failure;
    public StubMlClient setResume(MlExtraction value) { resume = value; return this; }
    public StubMlClient setCertificate(MlExtraction value) { certificate = value; return this; }
    public StubMlClient failWith(RuntimeException value) { failure = value; return this; }
    @Override public MlExtraction extractResume(byte[] content, String filename, String contentType) { if (failure != null) throw failure; return resume; }
    @Override public MlExtraction extractCertificate(byte[] content, String filename, String contentType) { if (failure != null) throw failure; return certificate; }
}
