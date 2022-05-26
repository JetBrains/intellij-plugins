package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.*;

public final class MeteorStubPath {
  
  @NotNull
  public static VirtualFile getLastMeteorLib() {
    return Arrays.stream(getStubDir().getChildren())
      .filter(file1 -> file1.getName().endsWith(TypeScriptUtil.TYPESCRIPT_DECLARATIONS_FILE_EXTENSION))
      .map(VFWrapper::new)
      .max(Comparator.naturalOrder())
      .orElseThrow()
      .myFile;
  }

  /**
   * File name format meteor-vXX.XX.XX.d.ts
   */
  static final class VFWrapper implements Comparable<VFWrapper> {
    private final VirtualFile myFile;

    private final VersionNumber myFileVersion;

    VFWrapper(@NotNull VirtualFile file) {
      myFile = file;
      myFileVersion = new VersionNumber(file.getName());
    }
    @Override
    public int compareTo(@NotNull VFWrapper o) {
      return myFileVersion.compareTo(o.myFileVersion);
    }
  }

  static final class VersionNumber implements Comparable<VersionNumber> {
    final List<Integer> myOrderedVersions = new ArrayList<>();
    String myFileName;

    VersionNumber(@NotNull String fileName) {
      myFileName = fileName;
      String versionString =
        fileName.substring(fileName.indexOf('-') + 2, fileName.lastIndexOf(TypeScriptUtil.TYPESCRIPT_DECLARATIONS_FILE_EXTENSION));
      for (String s : versionString.split("\\.")) {
        myOrderedVersions.add(Integer.valueOf(s));
      }
    }

    @Override
    public int compareTo(@NotNull VersionNumber o) {
      int index = 0;
      int size1 = myOrderedVersions.size();
      int size2 = o.myOrderedVersions.size();
      while (index < size2 && index < size1) {
        Integer v1 = myOrderedVersions.get(index);
        Integer v2 = o.myOrderedVersions.get(index);
        int compare = v1.compareTo(v2);
        if (compare != 0) {
          return compare;
        }
        index++;
      }
      return Integer.compare(size1, size2);
    }
  }

  public static @NotNull VirtualFile getStubDir() {
    URL url = Objects.requireNonNull(MeteorStubPath.class.getClassLoader().getResource("tsMeteorStubs"));
    return Objects.requireNonNull(VfsUtil.findFileByURL(url));
  }
}

