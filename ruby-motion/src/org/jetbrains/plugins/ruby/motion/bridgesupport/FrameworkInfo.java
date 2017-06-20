/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.reference.SoftReference;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;

import java.io.IOException;
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
      idSelectorNames = ContainerUtil.newHashSet();
      selectorNames = ContainerUtil.newHashSet();
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
