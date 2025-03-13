// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.idea.perforce;

public class ClientVersion {
  public static boolean DISABLE_MOVE_IN_TESTS = false;
  public static ClientVersion UNKNOWN = new ClientVersion(-1, -1, -1);

  private final long myYear;
  private final long myVersion;
  private final long myBuild;

  public ClientVersion(long year, long version, long build) {
    myYear = year;
    myVersion = version;
    myBuild = build;
  }

  public boolean supportsMove() {
    if (DISABLE_MOVE_IN_TESTS) {
      return false;
    }

    return myYear > 2009 || myYear == 2009 && myVersion > 1;
  }

  public boolean supportsP4vcParam() {
    return myYear >= 2014 && myVersion >= 1;
  }

  @Override
  public String toString() {
    return myYear + "." + myVersion + "/" + myBuild;
  }
}
