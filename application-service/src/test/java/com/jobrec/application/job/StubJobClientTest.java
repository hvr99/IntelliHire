package com.jobrec.application.job;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class StubJobClientTest {
    @Test void returnsConfiguredJob() { UUID id=UUID.randomUUID(), recruiter=UUID.randomUUID(); JobDetails actual=new StubJobClient().addJob(id,recruiter,JobStatus.OPEN).getJob(id); assertEquals(recruiter,actual.recruiterId()); assertEquals(JobStatus.OPEN,actual.status()); }
    @Test void simulatesDownstreamFailure() { StubJobClient stub=new StubJobClient().failWith(new JobServiceUnavailableException("down",null)); assertThrows(JobServiceUnavailableException.class, () -> stub.getJob(UUID.randomUUID())); }
}
