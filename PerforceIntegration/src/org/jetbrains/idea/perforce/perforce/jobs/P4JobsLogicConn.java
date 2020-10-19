package org.jetbrains.idea.perforce.perforce.jobs;

import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public class P4JobsLogicConn {
  private final P4Connection myConnection;
  private final PerforceJobSpecification mySpec;
  private final String myJobView;

  public P4JobsLogicConn(P4Connection connection, PerforceJobSpecification spec, String jobView) {
    myConnection = connection;
    mySpec = spec;
    myJobView = jobView;
  }

  public P4Connection getConnection() {
    return myConnection;
  }

  public PerforceJobSpecification getSpec() {
    return mySpec;
  }

  public String getJobView() {
    return myJobView;
  }
}
