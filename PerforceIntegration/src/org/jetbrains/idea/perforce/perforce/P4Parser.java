package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.vcs.VcsException;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class P4Parser {
  private final @NotNull RevisionCollector myRevisionCollector;
  private final @NotNull P4Command myCommand;

  public P4Parser(@NotNull P4Command command, @NotNull Object2LongMap<String> revisions) {
    myRevisionCollector = new RevisionCollector(revisions);
    myCommand = command;
  }

  public @NotNull P4Command getCommand() {
    return myCommand;
  }

  protected abstract @Nullable ParsedLine consumeLine(@NotNull String outputLine) throws VcsException;

  protected void consumeRevision(@NotNull String path, long revision) {
    myRevisionCollector.consumeRevision(path, revision);
  }

  void readOutput(@NotNull InputStream inputStream) throws IOException, VcsException {
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    do {
      String line = reader.readLine();
      if (line == null || line.isEmpty()) break;
      ParsedLine parsedLine = consumeLine(line);
      if (parsedLine != null) {
        consumeRevision(parsedLine.path, parsedLine.revision);
      }
    }
    while (true);
  }

  static final class RevisionCollector {
    private final Object2LongMap<String> myRevisions;

    RevisionCollector(@NotNull Object2LongMap<String> revisions) {
      myRevisions = revisions;
    }

    void consumeRevision(@NotNull String path, long revision) {
      myRevisions.put(path, revision);
    }
  }

  protected record ParsedLine(@NotNull String path, long revision) {
  }
}

