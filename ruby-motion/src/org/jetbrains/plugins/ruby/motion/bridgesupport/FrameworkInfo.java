// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FrameworkInfo {
  private static final Logger LOG = Logger.getInstance(FrameworkInfo.class);
  @NotNull private final String myBridgeSupportPath;
  @NotNull private final String myName;
  @NotNull private final String myVersion;
  private final boolean myOSX;

  private SoftReference<Framework> myFramework = null;
  private SoftReference<Set<String>> myIdSelectorNames = null;
  private SoftReference<Set<String>> mySelectorNames = null;

  public FrameworkInfo(@NotNull String name, @NotNull String version, boolean isOSX, @NotNull String bridgeSupportFilePath) {
    myBridgeSupportPath = bridgeSupportFilePath;
    myName = name;
    myOSX = isOSX;
    myVersion = version;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Nullable
  public synchronized Framework getFramework() {
    Framework result = SoftReference.dereference(myFramework);
    return result != null ? result : reloadFramework().first;
  }

  @Nullable
  public synchronized Set<String> getIdSelectorNames() {
    Set<String> result = SoftReference.dereference(myIdSelectorNames);
    return result != null ? result : reloadFramework().second;
  }

  @Nullable
  public synchronized Set<String> getSelectorNames() {
    Set<String> result = SoftReference.dereference(mySelectorNames);
    return result != null ? result : reloadFramework().third;
  }

  private Trinity<Framework, Set<String>, Set<String>> reloadFramework() {
    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(myBridgeSupportPath);
    if (file == null) return Trinity.create(null, null, null);

    Set<String> idSelectorNames = null;
    Set<String> selectorNames = null;
    Framework framework = null;
    try {
      framework = BridgeSupportReader.read(myName, myVersion, file.getInputStream(), myOSX);
      idSelectorNames = new HashSet<>();
      selectorNames = new HashSet<>();
      for (Class clazz : framework.getClasses()) {
        for (Function function : clazz.getFunctions()) {
          if (function.isId()) {
            idSelectorNames.addAll(MotionSymbolUtil.getSelectorNames(function));
          }
          selectorNames.add(function.getName());
        }
      }
    }
    catch (IOException e) {
      LOG.error("Failed to load bridgesupport file", e);
    }
    myFramework = framework != null ? new SoftReference<>(framework) : null;
    myIdSelectorNames = idSelectorNames != null ? new SoftReference<>(idSelectorNames) : null;
    mySelectorNames = selectorNames != null ? new SoftReference<>(selectorNames) : null;
    return Trinity.create(framework, idSelectorNames, selectorNames);
  }
}
