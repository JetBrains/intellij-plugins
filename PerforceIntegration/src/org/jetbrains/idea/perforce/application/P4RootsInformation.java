package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.idea.perforce.perforce.PerforceAuthenticationException;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.Map;

public interface P4RootsInformation {
  MultiMap<P4Connection, VcsException> getErrors();
  boolean hasAnyErrors();
  boolean hasNotAuthorized();
  Map<P4Connection, PerforceAuthenticationException> getNotAuthorized();
  Map<P4Connection, PerforceClientRootsChecker.WrongRoots> getMap();
}
