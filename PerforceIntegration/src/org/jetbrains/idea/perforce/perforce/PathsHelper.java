package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.vcs.FilePath;
import org.jetbrains.idea.perforce.application.PerforceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PathsHelper {
  private final PerforceManager myPerforceManager;
  private final Collection<FilePath> myRecursivePaths = new ArrayList<>();
  private final Collection<FilePath> mySimplePaths = new ArrayList<>();

  public PathsHelper(final PerforceManager perforceManager) {
    myPerforceManager = perforceManager;
  }

  public void add(final FilePath path) {
    mySimplePaths.add(path);
  }

  public void addRecursively(final FilePath path) {
    myRecursivePaths.add(path);
  }

  public void addAllPaths(final Collection<FilePath> files) {
    mySimplePaths.addAll(files);
  }

  public boolean isEmpty() {
    return myRecursivePaths.isEmpty() && mySimplePaths.isEmpty();
  }

  public List<String> getRequestString() {
    final List<String> result = new ArrayList<>();
    for (FilePath file : myRecursivePaths) {
      result.add(convert(PerforceRunner.getP4FilePath(P4File.create(file), file.isDirectory(), true)));
    }
    for (FilePath file : mySimplePaths) {
      result.add(convert(PerforceRunner.getP4FilePath(P4File.create(file), file.isDirectory(), false)));
    }
    return result;
  }

  private String convert(final String s) {
    return myPerforceManager.convertP4ParsedPath(null, s);
  }
}
