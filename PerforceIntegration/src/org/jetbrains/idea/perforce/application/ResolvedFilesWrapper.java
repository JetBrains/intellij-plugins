// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.idea.perforce.application;

import org.jetbrains.idea.perforce.perforce.ResolvedFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolvedFilesWrapper {
  private final Map<File, ResolvedFile> myLocalToFiles;
  private final Map<String, ResolvedFile> myDepotToFiles;

  public ResolvedFilesWrapper(final List<ResolvedFile> resolvedFiles) {
    myLocalToFiles = new HashMap<>();
    myDepotToFiles = new HashMap<>();

    for (ResolvedFile file : resolvedFiles) {
      myLocalToFiles.put(file.getLocalFile(), file);
      myDepotToFiles.put(file.getDepotPath(), file);
    }
  }

  public Map<File, ResolvedFile> getLocalToFiles() {
    return myLocalToFiles;
  }

  public Map<String, ResolvedFile> getDepotToFiles() {
    return myDepotToFiles;
  }
}
