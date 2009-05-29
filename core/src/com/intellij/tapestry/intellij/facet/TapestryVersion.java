package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.ui.libraries.LibraryInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createSubMavenJarInfo;

public enum TapestryVersion {

  TAPESTRY_5_1_0_5("5.1.0.5", new LibraryInfo[]{
      createSubMavenJarInfo("org/apache/tapestry", "tapestry-core", "5.1.0.5", "org.apache.tapestry5.corelib.components.Loop"),
      createSubMavenJarInfo("org/apache/tapestry", "tapestry-ioc", "5.1.0.5", "org.apache.tapestry5.ioc.Registry"),
      createSubMavenJarInfo("org/apache/tapestry", "tapestry5-annotations", "5.1.0.5", "org.apache.tapestry5.beaneditor.Validate"),
  });

  private final String _name;
  private final LibraryInfo[] _jars;

  private TapestryVersion(String name, LibraryInfo[] jars) {
    _name = name;
    _jars = jars;
  }

  public LibraryInfo[] getJars() {
    return _jars;
  }

  public String toString() {
    return _name;
  }

  public static TapestryVersion fromString(String name) {
    for (TapestryVersion version : TapestryVersion.values()) {
      if (version.toString().equals(name)) return version;
    }
    return null;
  }


}
