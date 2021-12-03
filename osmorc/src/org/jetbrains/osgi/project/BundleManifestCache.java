// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.project;

import aQute.bnd.osgi.Constants;
import aQute.lib.utf8properties.UTF8Properties;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JdkOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderEx;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.jetbrains.osgi.bnd.imp.BndProjectImporter;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class BundleManifestCache {
  public static BundleManifestCache getInstance(@NotNull Project project) {
    return project.getService(BundleManifestCache.class);
  }

  private static final Key<CachedValue<BundleManifest>> MANIFEST_CACHE_KEY = Key.create("osgi.bundle.manifest.cache");

  @Nullable
  public BundleManifest getManifest(@NotNull PsiClass psiClass) {
    PsiFile classOwner = psiClass.getContainingFile();
    return classOwner != null ? getManifest(classOwner) : null;
  }

  @Nullable
  public BundleManifest getManifest(@NotNull PsiFileSystemItem item) {
    VirtualFile file = item.getVirtualFile();
    if (file != null) {
      ProjectFileIndex index = ProjectFileIndex.getInstance(item.getProject());
      List<OrderEntry> entries = index.getOrderEntriesForFile(file);
      if (entries.size() == 1 && entries.get(0) instanceof JdkOrderEntry) {
        return new JdkBundleManifest();
      }

      Module module = index.getModuleForFile(file);
      if (module != null) {
        return getManifest(module);
      }

      VirtualFile libRoot = index.getClassRootForFile(file);
      if (libRoot != null) {
        return getManifest(libRoot, item.getManager());
      }
    }

    return null;
  }

  @Nullable
  public BundleManifest getManifest(@NotNull Module module) {
    OsmorcFacet facet = OsmorcFacet.getInstance(module);
    if (facet == null) return null;

    CachedValue<BundleManifest> value = facet.getUserData(MANIFEST_CACHE_KEY);

    if (value == null) {
      value = facet.putUserDataIfAbsent(MANIFEST_CACHE_KEY, CachedValuesManager.getManager(module.getProject()).createCachedValue(() -> {
        OsmorcFacetConfiguration configuration = facet.getConfiguration();
        BundleManifest manifest = null;
        List<Object> dependencies = new SmartList<>(configuration);

        switch (configuration.getManifestGenerationMode()) {
          case Manually: {
            PsiFile manifestFile = findInModuleRoots(facet.getModule(), configuration.getManifestLocation());
            if (manifestFile instanceof ManifestFile) {
              manifest = readManifest((ManifestFile)manifestFile);
              dependencies.add(manifestFile);
            }
            else {
              dependencies.add(PsiModificationTracker.MODIFICATION_COUNT);
            }
            break;
          }

          case OsmorcControlled: {
            Map<String, String> map = new HashMap<>(configuration.getAdditionalPropertiesAsMap());
            map.put(Constants.BUNDLE_SYMBOLICNAME, configuration.getBundleSymbolicName());
            map.put(Constants.BUNDLE_VERSION, configuration.getBundleVersion());
            map.put(Constants.BUNDLE_ACTIVATOR, configuration.getBundleActivator());
            manifest = new BundleManifest(map);
            break;
          }

          case Bnd: {
            PsiFile bndFile = findInModuleRoots(facet.getModule(), configuration.getBndFileLocation());
            if (bndFile != null) {
              manifest = readProperties(bndFile);
              dependencies.add(bndFile);
            }
            else {
              dependencies.add(PsiModificationTracker.MODIFICATION_COUNT);
            }
            break;
          }

          case Bundlor:
            break; // not supported
        }

        return CachedValueProvider.Result.create(manifest, dependencies);
      }, false));
    }

    return value.getValue();
  }

  @Nullable
  public BundleManifest getManifest(@NotNull VirtualFile libRoot, @NotNull PsiManager manager) {
    PsiDirectory psiRoot = manager.findDirectory(libRoot);
    if (psiRoot == null) return null;

    CachedValue<BundleManifest> value = libRoot.getUserData(MANIFEST_CACHE_KEY);
    if (value == null) {
      value = ((UserDataHolderEx)psiRoot).putUserDataIfAbsent(MANIFEST_CACHE_KEY, CachedValuesManager.getManager(manager.getProject()).createCachedValue(() -> {
        PsiDirectory metaInfDir = psiRoot.findSubdirectory("META-INF");
        PsiFile psiFile = metaInfDir != null ? metaInfDir.findFile("MANIFEST.MF") : null;
        BundleManifest manifest = psiFile instanceof ManifestFile ? readManifest((ManifestFile)psiFile) : null;
        return CachedValueProvider.Result.createSingleDependency(manifest, Objects.requireNonNullElse(psiFile, libRoot));
      }, false));
    }
    return value.getValue();
  }

  private static PsiFile findInModuleRoots(Module module, String path) {
    for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
      VirtualFile file = root.findFileByRelativePath(path);
      if (file != null) {
        return PsiManager.getInstance(module.getProject()).findFile(file);
      }
    }

    return null;
  }

  private static BundleManifest readManifest(ManifestFile manifestFile) {
    try {
      ByteArrayInputStream stream = new ByteArrayInputStream(manifestFile.getText().getBytes(StandardCharsets.UTF_8));
      Attributes attributes = new Manifest(stream).getMainAttributes();
      Map<String, String> map = new HashMap<>();
      for (Object key : attributes.keySet()) {
        String name = key.toString();
        map.put(name, attributes.getValue(name));
      }
      return new BundleManifest(map, manifestFile);
    }
    catch (IOException ignored) { }
    catch (InvalidVirtualFileAccessException ignored) { }

    return null;
  }

  private static BundleManifest readProperties(PsiFile propertiesFile) {
    try {
      UTF8Properties properties = new UTF8Properties();
      properties.load(new StringReader(propertiesFile.getText()));
      Map<String, String> map = new HashMap<>();
      for (Object key : properties.keySet()) {
        String name = key.toString();
        map.put(name, properties.getProperty(name));
      }
      if (map.get(Constants.BUNDLE_SYMBOLICNAME) == null) {
        VirtualFile file = propertiesFile.getVirtualFile();
        if (file != null) {
          if (!BndProjectImporter.BND_FILE.equals(file.getName())) {
            map.put(Constants.BUNDLE_SYMBOLICNAME, FileUtilRt.getNameWithoutExtension(file.getName()));
          }
          else if (file.getParent() != null) {
            map.put(Constants.BUNDLE_SYMBOLICNAME, file.getParent().getName());
          }
        }
      }
      return new BundleManifest(map, propertiesFile);
    }
    catch (IOException ignored) { }
    catch (InvalidVirtualFileAccessException ignored) { }

    return null;
  }

  private static class JdkBundleManifest extends BundleManifest {
    JdkBundleManifest() {
      super(Collections.emptyMap());
    }

    @Override
    public String getBundleSymbolicName() {
      return "";
    }

    @Override
    public String getExportedPackage(@NotNull String packageName) {
      return packageName;
    }
  }
}
