/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.merge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseRevision {
  private final String myRevisionNum;
  private final String mySourceRevision;
  private final String myDepotPath;

  public BaseRevision(@Nullable final String revisionNum, @Nullable final String sourceRevision, @NotNull final String depotPath) {
    myDepotPath = depotPath;
    mySourceRevision = sourceRevision;
    myRevisionNum = revisionNum;
  }

  @Nullable
  public String getRevisionNum() {
    return myRevisionNum;
  }

  @Nullable
  public String getSourceRevision() {
    return mySourceRevision;
  }

  @NotNull
  public String getDepotPath() {
    return myDepotPath;
  }
}
