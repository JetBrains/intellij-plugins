// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.utils.VirtualFileUtil;

import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class BridgeSupportLoader {
  private final Map<String, List<FrameworkInfo>> myFrameworks = new HashMap<>();

  public static BridgeSupportLoader getInstance() {
    return ServiceManager.getService(BridgeSupportLoader.class);
  }

  @Nullable
  public Framework getFramework(String version, String name) {
    final FrameworkInfo frameworkInfo = getFrameworkInfo(version, name);
    return frameworkInfo != null ? frameworkInfo.getFramework() : null;
  }

  @Nullable
  private FrameworkInfo getFrameworkInfo(String version, String name) {
    ensureFrameworksLoaded();
    final List<FrameworkInfo> frameworkInfos = myFrameworks.get(version);
    if (frameworkInfos == null) return null;

    for (FrameworkInfo frameworkInfo : frameworkInfos) {
      if (frameworkInfo.getName().equals(name)) {
        return frameworkInfo;
      }
    }
    return null;
  }

  private synchronized void ensureFrameworksLoaded() {
    if (!myFrameworks.isEmpty()) {
      return;
    }
    VirtualFile rubyMotion = VirtualFileUtil.findFileBy(RubyMotionUtil.getInstance().getRubyMotionPath() + "/data/ios/");
    rubyMotion = rubyMotion == null ? VirtualFileUtil.findFileBy(RubyMotionUtil.getInstance().getRubyMotionPath() + "/data/") : rubyMotion;
    loadSdks(rubyMotion);
    if (rubyMotion == null || !"ios".equals(rubyMotion.getName())) {
      return;
    }
    rubyMotion = VirtualFileUtil.findFileBy(RubyMotionUtil.getInstance().getRubyMotionPath() + "/data/android/");
    loadSdks(rubyMotion);
    rubyMotion = VirtualFileUtil.findFileBy(RubyMotionUtil.getInstance().getRubyMotionPath() + "/data/osx/");
    loadSdks(rubyMotion);
  }

  private void loadSdks(VirtualFile rubyMotion) {
    if (rubyMotion == null) {
      return;
    }
    for (VirtualFile file : rubyMotion.getChildren()) {
      if (file.isDirectory()) {
        final VirtualFile bridgeSupport = file.findChild("BridgeSupport");
        if (bridgeSupport == null) continue;
        ensureFrameworkLoaded(bridgeSupport);
      }
    }
  }

  private void ensureFrameworkLoaded(final VirtualFile bridgeSupport) {
    String version = bridgeSupport.getParent().getName();
    List<FrameworkInfo> frameworkInfos = new ArrayList<>();
    for (VirtualFile file : bridgeSupport.getChildren()) {
      final String name = file.getNameWithoutExtension();
      if (RubyMotionUtil.getInstance().isIgnoredFrameworkName(name)) continue;
      if ("bridgesupport".equals(file.getExtension()) && file.getLength() > 0) {
        boolean isOSX = "osx".equals(file.getParent().getParent().getParent().getName());
        frameworkInfos.add(new FrameworkInfo(name, version, isOSX, file.getPath()));
      }
    }
    myFrameworks.put(version, frameworkInfos);
  }

  public boolean isIdSelector(String name, String sdkVersion, String frameworkName) {
    final FrameworkInfo info = getFrameworkInfo(sdkVersion, frameworkName);
    if (info == null) return false;
    Set<String> selectors = info.getIdSelectorNames();
    return selectors != null && selectors.contains(name);
  }

  public boolean isSelector(String name, String sdkVersion, String frameworkName) {
    final FrameworkInfo info = getFrameworkInfo(sdkVersion, frameworkName);
    if (info == null) return false;
    Set<String> selectors = info.getSelectorNames();
    return selectors != null && selectors.contains(name);
  }

  public void processFrameworks(Consumer<Framework> consumer) {
    ensureFrameworksLoaded();
    for (List<FrameworkInfo> frameworkInfos : myFrameworks.values()) {
      for (FrameworkInfo frameworkInfo : frameworkInfos) {
        Framework framework = frameworkInfo.getFramework();
        if (framework != null) {
          consumer.consume(framework);
        }
      }
    }
  }
}
