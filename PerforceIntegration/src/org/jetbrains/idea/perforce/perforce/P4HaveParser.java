package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class P4HaveParser {
  private static final String HAVE_DELIMITER = " - ";
  private final PerforceManager myPerforceManager;

  public P4HaveParser(PerforceManager perforceManager) {
    myPerforceManager = perforceManager;
  }

  public void consumeLine(final String haveLine) throws VcsException {
    final int hashIndex = haveLine.indexOf('#');
    if (hashIndex < 0) {
      throw new VcsException(PerforceBundle.message("error.unexpected.p4.have.output.format", haveLine));
    }
    final int idx = haveLine.indexOf(HAVE_DELIMITER, hashIndex);
    if (idx < 0) {
      throw new VcsException(PerforceBundle.message("error.unexpected.p4.have.output.format", haveLine));
    }
    String localPath = haveLine.substring(idx + HAVE_DELIMITER.length());
    localPath = myPerforceManager.convertP4ParsedPath(null, localPath);
    final long revision = Long.parseLong(haveLine.substring(hashIndex+1, idx));
    consumeRevision(FileUtil.toSystemDependentName(localPath), revision);
  }

  public abstract void consumeRevision(String path, long revision);

  void readHaveOutput(InputStream inputStream) throws IOException, VcsException {
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    do {
      String line = reader.readLine();
      if (line == null || line.length() == 0) break;
      consumeLine(line);
    }
    while (true);
  }

  static final class RevisionCollector extends P4HaveParser {
    private final Object2LongMap<String> myHaveRevisions;

    RevisionCollector(PerforceManager perforceManager, @NotNull Object2LongMap<String> haveRevisions) {
      super(perforceManager);
      myHaveRevisions = haveRevisions;
    }

    @Override
    public void consumeRevision(String path, long revision) {
      myHaveRevisions.put(path, revision);
    }
  }
}

