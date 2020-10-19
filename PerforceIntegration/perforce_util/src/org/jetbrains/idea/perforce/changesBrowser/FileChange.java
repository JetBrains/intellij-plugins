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
package org.jetbrains.idea.perforce.changesBrowser;

import org.jetbrains.idea.perforce.perforce.PerforceAbstractChange;
import org.jetbrains.annotations.NonNls;

import java.io.File;

public class FileChange extends PerforceAbstractChange{
  private final long myFileRevision;
  private final String myDepotPath;
  @NonNls public static final String DELETE_ACTION = "delete";
  @NonNls public static final String ADD_ACTION = "add";
  @NonNls public static final String EDIT_ACTION = "edit";
  @NonNls public static final String BRANCH_ACTION = "branch";
  @NonNls public static final String INTEGRATE_ACTION = "integrate";
  @NonNls public static final String MOVE_DELETE_ACTION = "move/delete";
  @NonNls public static final String MOVE_ADD_ACTION = "move/add";

  public FileChange(String depotPath,final File file, final long fileRevision, final String changeType) {
    myFileRevision = fileRevision;
    setFile(file);
    setType(changeType);
    myDepotPath = depotPath;
  }

  public String getDepotPath() {
    return myDepotPath;
  }

  public long getRevisionAfter() {
    return myFileRevision;    
  }

  @Override @NonNls
  public String toString() {
    return "perforce.FileChange[depotPath=" + myDepotPath + ",changeType=" + getType() + "]";
  }
}
