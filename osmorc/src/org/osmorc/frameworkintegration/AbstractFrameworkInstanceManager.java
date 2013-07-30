package org.osmorc.frameworkintegration;

import com.intellij.openapi.util.io.JarUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.util.OsgiFileUtil;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all {@link FrameworkInstanceManager}s.
 */
public abstract class AbstractFrameworkInstanceManager implements FrameworkInstanceManager {
  @Nullable
  public String getVersion(@NotNull FrameworkInstanceDefinition instance) {
    Collection<SelectedBundle> bundles = getFrameworkBundles(instance, FrameworkBundleType.SYSTEM);
    if (bundles.size() == 1) {
      SelectedBundle bundle = bundles.iterator().next();
      return CachingBundleInfoProvider.getBundleVersions(bundle.getBundleUrl());
    }

    return null;
  }

  @Nullable
  @Override
  public String checkValidity(@NotNull FrameworkInstanceDefinition instance) {
    String basePath = instance.getBaseFolder();
    if (basePath == null || !new File(basePath).isDirectory()) {
      return OsmorcBundle.message("framework.directory.missing", (basePath != null ? basePath : ""), instance.getFrameworkIntegratorName());
    }

    String version = getVersion(instance);
    if (version == null) {
      return OsmorcBundle.message("framework.jar.missing", basePath, instance.getFrameworkIntegratorName());
    }

    if (StringUtil.isEmptyOrSpaces(instance.getName())) {
      return OsmorcBundle.message("framework.name.missing");
    }

    return null;
  }

  // todo[r.sh] move to interface; replace old collector
  @NotNull
  public Collection<SelectedBundle> getFrameworkBundles(@NotNull FrameworkInstanceDefinition instance, @NotNull FrameworkBundleType type) {
    String basePath = instance.getBaseFolder();
    if (basePath == null || !new File(basePath).isDirectory()) return Collections.emptyList();

    List<File> dirs = ContainerUtil.newArrayList();
    for (String subDir : getBundleDirectories()) {
      if (subDir.isEmpty()) {
        dirs.add(new File(basePath));
      }
      else if (subDir.endsWith("/*")) {
        File[] nestedDirs = new File(basePath, subDir.substring(0, subDir.length() - 2)).listFiles();
        if (nestedDirs != null) {
          ContainerUtil.addAll(dirs, nestedDirs);
        }
      }
      else {
        dirs.add(new File(basePath, subDir));
      }
    }

    Collection<SelectedBundle> bundles = ContainerUtil.newArrayList();
    outer:
    for (File dir : dirs) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.getName().endsWith(".jar") && JarUtil.containsEntry(file, "META-INF/MANIFEST.MF")) {
            Result result = checkType(file, type);
            if (result != Result.NOT_A) {
              SelectedBundle bundle = makeBundle(file, type);
              bundles.add(bundle);
            }
            if (result == Result.IS_A) {
              break outer;
            }
          }
        }
      }
    }

    return bundles;
  }

  @NotNull
  protected abstract String[] getBundleDirectories();

  protected enum Result {
    IS_A, IS_ONE_OF, NOT_A;

    public static Result isA(boolean result) { return result ? IS_A : NOT_A; }
    public static Result oneOf(boolean result) { return result ? IS_ONE_OF : NOT_A; }
  }

  protected @NotNull Result checkType(@NotNull File file, @NotNull FrameworkBundleType type) {
    return Result.oneOf(type == FrameworkBundleType.OTHER);
  }

  protected SelectedBundle makeBundle(@NotNull File file, @NotNull FrameworkBundleType type) {
    String url = OsgiFileUtil.pathToUrl(file.getPath());

    String bundleName = CachingBundleInfoProvider.getBundleSymbolicName(url);
    if (bundleName != null) {
      String bundleVersion = CachingBundleInfoProvider.getBundleVersions(url);
      if (bundleVersion != null) {
        bundleName += " - " + bundleVersion;
      }
    }
    else {
      bundleName = file.getName();
    }

    return new SelectedBundle(bundleName, url, SelectedBundle.BundleType.FrameworkBundle);
  }
}
