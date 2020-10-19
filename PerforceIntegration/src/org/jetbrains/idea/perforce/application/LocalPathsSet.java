package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LocalPathsSet {
  private final Set<String> mySet = new HashSet<>();

  public LocalPathsSet(Collection<String> paths) {
    for (String path : paths) {
      mySet.add(convert(path));
    }
  }

  private static String convert(final String value) {
    return (SystemInfo.isFileSystemCaseSensitive ? value : StringUtil.toLowerCase(value)).replace("\\", "/");
  }

  public boolean contains(final File file) {
    final String modified = convert(file.getAbsolutePath());
    return mySet.contains(modified);
  }

}
