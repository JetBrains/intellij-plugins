package com.google.dart.server;

import org.dartlang.analysis.server.protocol.DartLspApplyWorkspaceEditResult;

public interface DartLspWorkspaceApplyEditRequestConsumer extends Consumer {
  public void workspaceEditApplied(DartLspApplyWorkspaceEditResult result);
}
