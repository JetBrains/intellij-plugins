// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.facet;

import com.intellij.framework.library.DownloadableLibraryType;
import com.intellij.framework.library.LibraryVersionProperties;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibrariesHelper;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtilRt;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class OsgiCoreLibraryType extends DownloadableLibraryType {
  private static final String ID = "org.osgi.core";
  private static final String DETECTOR_CLASS = "org.osgi.framework.Constants";

  public OsgiCoreLibraryType() {
    super(OsmorcBundle.messagePointer("facet.library.name"), ID, ID, OsgiCoreLibraryType.class.getResource("osgi.core.xml"));
  }

  @Override
  public LibraryVersionProperties detect(@NotNull List<VirtualFile> roots) {
    if (!LibraryUtil.isClassAvailableInLibrary(roots, DETECTOR_CLASS)) {
      return null;
    }

    VirtualFile jar = LibrariesHelper.getInstance().findRootByClass(roots, DETECTOR_CLASS);
    if (jar != null && jar.getFileSystem() instanceof JarFileSystem) {
      VirtualFile manifestFile = jar.findFileByRelativePath(JarFile.MANIFEST_NAME);
      if (manifestFile != null) {
        try {
          InputStream input = manifestFile.getInputStream();
          try {
            String version = new Manifest(input).getMainAttributes().getValue(Constants.BUNDLE_VERSION);
            if (version != null) {
              try {
                Version v = new Version(version);
                return new LibraryVersionProperties(v.getMajor() + "." + v.getMinor() + "." + v.getMicro());
              }
              catch (IllegalArgumentException ignored) { }
            }
          }
          finally {
            input.close();
          }
        }
        catch (IOException ignored) { }
      }
    }

    // unknown version
    return new LibraryVersionProperties(null);
  }

  @NotNull
  @Override
  public Icon getLibraryTypeIcon() {
    return OsmorcIdeaIcons.Osgi;
  }

  @Override
  protected String @NotNull [] getDetectionClassNames() {
    Logger.getInstance(getClass()).error(new AssertionError("shouldn't be called"));
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }

  public static boolean isOsgiCoreLibrary(@NotNull Library library) {
    VirtualFile[] roots = library.getFiles(OrderRootType.CLASSES);
    return LibraryUtil.isClassAvailableInLibrary(roots, DETECTOR_CLASS);
  }
}
