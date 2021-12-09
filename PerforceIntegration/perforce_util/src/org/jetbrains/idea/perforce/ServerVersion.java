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
package org.jetbrains.idea.perforce;

public class ServerVersion {
  private final long myVersionYear;
  private final long myVersionNum;

  public ServerVersion(final long versionYear, final long versionNum) {
    myVersionYear = versionYear;
    myVersionNum = versionNum;
  }

  public long getVersionYear() {
    return myVersionYear;
  }

  public long getVersionNum() {
    return myVersionNum;
  }

  private boolean isAtLeast(int year, int num) {
    return myVersionYear > year || myVersionYear == year && myVersionNum >= num;
  }

  public boolean supportsMove() {
    return isAtLeast(2009, 1);
  }
  
  public boolean supportsShelve() { return isAtLeast(2009, 1); }

  public boolean supportsIgnoresCommand() {
    return isAtLeast(2015, 2);
  }
}
