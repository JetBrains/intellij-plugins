/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
