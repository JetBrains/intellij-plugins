package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author irengrig
 */
public class P4RootsInformationHolder implements P4RootsInformation {
  private final MultiMap<P4Connection, VcsException> myExceptions;
  private final Map<P4Connection, PerforceClientRootsChecker.WrongRoots> myWrongRootsMap;
  private final Set<P4Connection> myNotAuthorized;

  public P4RootsInformationHolder(MultiMap<P4Connection, VcsException> exceptions,
                                  Map<P4Connection, PerforceClientRootsChecker.WrongRoots> wrongRootsMap, Set<P4Connection> notAuthorized) {
    myNotAuthorized = notAuthorized;
    myExceptions = new MultiMap<>();
    myExceptions.putAllValues(exceptions);
    myWrongRootsMap = new HashMap<>(wrongRootsMap);
  }

  @Override
  public MultiMap<P4Connection, VcsException> getErrors() {
    return myExceptions;
  }

  @Override
  public boolean hasAnyErrors() {
    return ! myExceptions.isEmpty() || ! myWrongRootsMap.isEmpty();
  }

  @Override
  public boolean hasNotAuthorized() {
    return ! myNotAuthorized.isEmpty();
  }

  @Override
  public Map<P4Connection, PerforceClientRootsChecker.WrongRoots> getMap() {
    return myWrongRootsMap;
  }

  @Override
  public Set<P4Connection> getNotAuthorized() {
    return myNotAuthorized;
  }
}
