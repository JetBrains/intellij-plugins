package org.jetbrains.idea.perforce;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.jobs.*;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.intellij.testFramework.UsefulTestCase.assertOneElement;
import static org.junit.Assert.*;

public class PerforceJobsTest extends PerforceTestCase {
  @Override
  public void before() throws Exception {
    super.before();

    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
  }

  @Test
  public void testJobsLoading() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    WriteAction.runAndWait(() -> refA.set(myWorkingCopyDir.createChildData(this, "a.txt")));
    assertNotNull(refA.get());

    addFile("a.txt");

    final String description = "123";

    final String jobName = "justdoit";
    final String secondJob = "another";
    final long listNumber = createChangeList(description, Collections.singletonList("//depot/a.txt"));
    checkNativeList(listNumber, description);

    createJob(jobName, "user", "open", "a task");
    linkJob(listNumber, jobName);

    createJob(secondJob, "user", "open", "a task");
    linkJob(listNumber, secondJob);

    refreshChanges();

    final LocalChangeList changeList = getChangeListManager().findChangeList(description);
    assertNotNull(changeList);

    final JobDetailsLoader loader = new JobDetailsLoader(myProject);
    final Map<ConnectionKey, P4JobsLogicConn> connMap = new HashMap<>();
    final Map<ConnectionKey, List<PerforceJob>> perforceJobs = new HashMap<>();
    loader.loadJobsForList(changeList, connMap, perforceJobs);

    final List<String> jobs = new ArrayList<>();
    final Collection<List<PerforceJob>> listCollection = perforceJobs.values();
    for (List<PerforceJob> jobList : listCollection) {
      for (PerforceJob job : jobList) {
        jobs.add(job.getName());
      }
    }

    assertEquals(2, jobs.size());
    assertTrue(jobs.contains(jobName));
    assertTrue(jobs.contains(secondJob));
  }

  @Test
  public void testJobSearchAll() throws Throwable {
    final String[][] data = {{"job001", "anna", "open", "once upon a time"},
      {"job002", "ivan", "open", "not a job actually"},
      {"yt-abs-3284", "john.smith", "closed", "time to say"}};

    for (String[] strings : data) {
      createJob(strings[0], strings[1], strings[2], strings[3]);
    }

    final JobsWorker worker = new JobsWorker(myProject);

    final FullSearchSpecificator spec = new FullSearchSpecificator();
    spec.addStandardConstraint(FullSearchSpecificator.Parts.jobname, "*");

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(PlatformTestUtil.getOrCreateProjectBaseDir(myProject));
    assertNotNull(connection);

    final PerforceJobSpecification jobsSpec = worker.getSpec(connection);
    final List<PerforceJob> perforceJobs = worker.getJobs(jobsSpec, spec, connection, new ConnectionKey("localhost", "test", "test"));

    assertEquals(3, perforceJobs.size());
    // in alphabet order
    assertEquals("job001", perforceJobs.get(0).getName());
  }

  @Test
  public void testJobSearchByName() throws Throwable {
    final String[][] data = {{"job001", "anna", "open", "once upon a time"},
      {"job002", "ivan", "open", "not a job actually"},
      {"a&&b", "ivan", "open", "not a job actually"},
      {"yt-abs-3284", "john.smith", "closed", "time to say"}};

    for (String[] strings : data) {
      createJob(strings[0], strings[1], strings[2], strings[3]);
    }

    final JobsWorker worker = new JobsWorker(myProject);

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(PlatformTestUtil.getOrCreateProjectBaseDir(myProject));
    assertNotNull(connection);
    ConnectionKey key = connection.getConnectionKey();
    final PerforceJobSpecification jobsSpec = worker.getSpec(connection);

    FullSearchSpecificator spec = new FullSearchSpecificator();
    spec.addStandardConstraint(FullSearchSpecificator.Parts.jobname, "job*");
    final List<PerforceJob> perforceJobs = worker.getJobs(jobsSpec, spec, connection, key);

    assertEquals(2, perforceJobs.size());
    // in alphabet order
    assertEquals("job001", perforceJobs.get(0).getName());
    assertEquals("job002", perforceJobs.get(1).getName());

    assertOneElement(worker.getJobs(jobsSpec, new ByNamesConstraint(Collections.singletonList("a&&b")), connection, key));
  }

  @Test
  public void testJobSearchBySeveralFields() throws Throwable {
    final String[][] data = {{"job001", "anna", "open", "once upon a time"},
      {"job002", "ivan", "open", "not a job actually"},
      {"yt-abs-3284", "john.smith", "closed", "time to say"}};

    for (String[] strings : data) {
      createJob(strings[0], strings[1], strings[2], strings[3]);
    }

    final JobsWorker worker = new JobsWorker(myProject);

    final FullSearchSpecificator spec = new FullSearchSpecificator();
    spec.addStandardConstraint(FullSearchSpecificator.Parts.description, "once upon*");
    spec.addStandardConstraint(FullSearchSpecificator.Parts.status, "closed");

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(PlatformTestUtil.getOrCreateProjectBaseDir(myProject));
    assertNotNull(connection);

    final PerforceJobSpecification jobsSpec = worker.getSpec(connection);
    final List<PerforceJob> perforceJobs = worker.getJobs(jobsSpec, spec, connection, new ConnectionKey("localhost", "test", "test"));

    assertEquals(0, perforceJobs.size());

    final FullSearchSpecificator spec2 = new FullSearchSpecificator();
    spec2.addStandardConstraint(FullSearchSpecificator.Parts.description, "once upon*");
    spec2.addStandardConstraint(FullSearchSpecificator.Parts.user, "a*a");

    final List<PerforceJob> perforceJobs2 = worker.getJobs(jobsSpec, spec2, connection, new ConnectionKey("localhost", "test", "test"));
    assertEquals(1, perforceJobs2.size());

    // in alphabet order
    assertEquals("job001", perforceJobs2.get(0).getName());
  }

  private void createJob(final String name, final String user, final String status, final String description) {
    final Map<String, List<String>> mapRepresentation = new HashMap<>();
    mapRepresentation.put(PerforceRunner.JOB, Collections.singletonList(name));
    mapRepresentation.put(PerforceRunner.USER, Collections.singletonList(user));
    mapRepresentation.put(PerforceRunner.STATUS, Collections.singletonList(status));
    mapRepresentation.put(PerforceRunner.DESCRIPTION, PerforceRunner.processDescription(description));

    final String spec = PerforceRunner.createStringFormRepresentation(mapRepresentation).toString();

    final ProcessOutput result = runP4(new String[]{"-c", "test", "job", "-i"}, spec);
    verify(result);
  }

  @Test
  public void testCreateListWithJobInNative() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    WriteAction.runAndWait(() -> refA.set(myWorkingCopyDir.createChildData(this, "a.txt")));
    assertNotNull(refA.get());

    addFile("a.txt");

    final String description = "123";

    final String jobName = "justdoit";
    final long listNumber = createChangeList(description, Collections.singletonList("//depot/a.txt"));
    checkNativeList(listNumber, description);

    createJob(jobName, "user", "open", "a task");
    linkJob(listNumber, jobName);

    refreshChanges();

    LocalChangeList changeList = getChangeListManager().findChangeList(description);
    assertNotNull(changeList);
    assertEquals(changeList.getComment(), description, changeList.getComment());
  }

  @Test
  public void testOptionalFields() throws VcsException {
    verify(runP4(new String[]{"-c", "test", "jobspec", "-i"}, """
      Fields:
      \t101 Job word 32 required
      \t102 Status word 32 optional
      \t103 User word 32 optional
      \t104 Date date 0 optional
      """));

    verify(runP4(new String[]{"-c", "test", "job", "-i"},
                 PerforceRunner.createStringFormRepresentation(ContainerUtil.newHashMap(
                   List.of(PerforceRunner.JOB),
                   List.of(Collections.singletonList("justdoit")))).toString()));

    final JobsWorker worker = new JobsWorker(myProject);

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(PlatformTestUtil.getOrCreateProjectBaseDir(myProject));
    assertNotNull(connection);
    ConnectionKey key = connection.getConnectionKey();

    FullSearchSpecificator spec = new FullSearchSpecificator();
    spec.addStandardConstraint(FullSearchSpecificator.Parts.jobname, "just*");
    PerforceJob job = assertOneElement(worker.getJobs(worker.getSpec(connection), spec, connection, key));

    assertEquals("justdoit", job.getName());
    assertNull(job.getValueForStandardField(StandardJobFields.date));
    assertNull(job.getValueForStandardField(StandardJobFields.user));
    assertNull(job.getValueForStandardField(StandardJobFields.status));
    assertNull(job.getValueForStandardField(StandardJobFields.description));
  }

}
