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
package org.jetbrains.idea.perforce.perforce;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.changesBrowser.FileChange;

import java.io.File;

public class PerforceAbstractChange {
  public static final int ADD = 0;
  public static final int DELETE = 1;
  public static final int EDIT = 2;
  public static final int BRANCH = 3;
  public static final int INTEGRATE = 4;
  // todo when get to know exactly where branch would be an equivalent to add, define isAdd() and isDelete() methods
  public static final int MOVE_ADD = 5;
  public static final int MOVE_DELETE = 6;
  public static final int UNKNOWN = -1;

  protected int myType = UNKNOWN;

  private File myFile;

  public PerforceAbstractChange() {
  }

  @Nullable
  public final File getFile() {
    return myFile;
  }

  public void setFile(final File file) {
    myFile = file;
  }

  public void setType(final String type) {
    myType = convertToType(type);
  }

  public static int convertToType(final String type) {
    if (FileChange.ADD_ACTION.equals(type)) {
      return ADD;
    } else if (FileChange.MOVE_ADD_ACTION.equals(type)) {
      return MOVE_ADD;
    }
    else if (FileChange.DELETE_ACTION.equals(type)) {
      return DELETE;
    }
    else if (FileChange.MOVE_DELETE_ACTION.equals(type)) {
      return MOVE_DELETE;
    }
    else if (FileChange.EDIT_ACTION.equals(type)) {
      return EDIT;
    }
    else if (FileChange.BRANCH_ACTION.equals(type)) {
      return BRANCH;
    }
    else if (FileChange.INTEGRATE_ACTION.equals(type)) {
      return INTEGRATE;
    }
    else {
      return UNKNOWN;
    }

  }


  public final int getType(){
    return myType;
  }
}
