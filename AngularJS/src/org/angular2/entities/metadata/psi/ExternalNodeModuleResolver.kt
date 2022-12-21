// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.index.Angular2MetadataNodeModuleIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.intellij.lang.javascript.library.JSLibraryUtil.NODE_MODULES;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX;

public class ExternalNodeModuleResolver {
  @NonNls private static final Logger LOG = Logger.getInstance(ExternalNodeModuleResolver.class);

  private static final Set<String> ourReportedErrors = new HashSet<>();
  private static final String NODE_MODULES_SEGMENT = "/" + NODE_MODULES + "/";

  private final Angular2MetadataElement mySource;
  private final String myModuleName;
  private final String myMemberName;

  public ExternalNodeModuleResolver(@NotNull Angular2MetadataElement source,
                                    @NotNull String moduleName,
                                    @Nullable String memberName) {
    mySource = source;
    myMemberName = memberName;
    myModuleName = moduleName;
  }

  public Angular2MetadataElement resolve() {
    Function<Angular2MetadataNodeModule, Angular2MetadataElement> memberExtractor =
      myMemberName == null ? nodeModule -> nodeModule
                           : nodeModule -> tryCast(nodeModule.findMember(myMemberName), Angular2MetadataElement.class);
    if (myModuleName.startsWith("./") || myModuleName.startsWith("../")) {
      Angular2MetadataNodeModule module = doIfNotNull(mySource.loadRelativeFile(myModuleName, METADATA_SUFFIX),
                                                      file -> PsiTreeUtil.getStubChildOfType(file, Angular2MetadataNodeModule.class));
      return module != null ? memberExtractor.apply(module) : null;
    }
    Angular2MetadataElement result = resolveFromFileSystem(memberExtractor);
    return result != null ? result : resolveFromIndex(memberExtractor);
  }

  private @Nullable Angular2MetadataElement resolveFromFileSystem(@NotNull Function<Angular2MetadataNodeModule, Angular2MetadataElement> memberExtractor) {
    return doIfNotNull(NodeModuleSearchUtil.findAncestorNodeModulesDir(mySource.getContainingFile().getVirtualFile()), dir -> {
      Angular2MetadataNodeModule module = doIfNotNull(mySource.loadRelativeFile(dir, myModuleName, METADATA_SUFFIX),
                                                      file -> PsiTreeUtil.getStubChildOfType(file, Angular2MetadataNodeModule.class));
      return module != null ? memberExtractor.apply(module) : null;
    });
  }

  private @Nullable Angular2MetadataElement resolveFromIndex(@NotNull Function<Angular2MetadataNodeModule, Angular2MetadataElement> memberExtractor) {
    MultiMap<Angular2MetadataElement, Angular2MetadataNodeModule> candidates = MultiMap.createSet();
    StubIndex.getInstance().processElements(
      Angular2MetadataNodeModuleIndex.KEY, myModuleName, mySource.getProject(),
      GlobalSearchScope.allScope(mySource.getProject()), Angular2MetadataNodeModule.class,
      nodeModule -> {
        if (nodeModule.isValid()) {
          doIfNotNull(memberExtractor.apply(nodeModule),
                      element -> {
                        candidates.putValue(element, nodeModule);
                        return null;
                      });
        }
        return true;
      });
    if (candidates.size() > 1) {
      retainReachableNodeModulesFolders(candidates);
    }
    if (candidates.size() > 1) {
      retainPackageTypingRoots(candidates);
    }
    if (candidates.size() > 1) {
      //noinspection OptionalGetWithoutIsPresent
      return StreamEx.of(candidates.keySet())
        // in case of multiple candidates, ensure deterministic outcome by using file path with lowest lexical order
        .min(Comparator.comparing(candidate -> candidate.getContainingFile().getVirtualFile().getPath()))
        .get();
    }
    return ContainerUtil.getFirstItem(candidates.keySet());
  }

  private void retainReachableNodeModulesFolders(@NotNull MultiMap<Angular2MetadataElement, Angular2MetadataNodeModule> candidates) {
    String path = getNodeModulesPath(mySource);
    if (path == null) {
      return;
    }
    Set<Angular2MetadataNodeModule> sameFolder = new HashSet<>();
    Set<Angular2MetadataNodeModule> parentFolder = new HashSet<>();
    candidates.values().forEach(nodeModule -> {
      String path1 = getNodeModulesPath(nodeModule);
      if (path1 != null) {
        if (path.equals(path1)) {
          sameFolder.add(nodeModule);
        }
        else if (path.startsWith(path1)) {
          parentFolder.add(nodeModule);
        }
      }
    });
    if (!sameFolder.isEmpty()) {
      retain(candidates, sameFolder);
    }
    else if (!parentFolder.isEmpty()) {
      retain(candidates, parentFolder);
    }
  }

  private static void retainPackageTypingRoots(@NotNull MultiMap<Angular2MetadataElement, Angular2MetadataNodeModule> candidates) {
    Set<Angular2MetadataNodeModule> packageTypingsRoots = new HashSet<>();
    candidates.values().forEach(nodeModule -> {
      if (nodeModule.isPackageTypingsRoot()) {
        packageTypingsRoots.add(nodeModule);
      }
    });
    if (!packageTypingsRoots.isEmpty()) {
      retain(candidates, packageTypingsRoots);
    }
  }

  private static void retain(@NotNull MultiMap<Angular2MetadataElement, Angular2MetadataNodeModule> candidates,
                             @NotNull Set<Angular2MetadataNodeModule> nodeModules) {
    Iterator<Map.Entry<Angular2MetadataElement, Collection<Angular2MetadataNodeModule>>> iterator = candidates.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Angular2MetadataElement, Collection<Angular2MetadataNodeModule>> entry = iterator.next();
      Iterator<Angular2MetadataNodeModule> listIterator = entry.getValue().iterator();
      while (listIterator.hasNext()) {
        if (!nodeModules.contains(listIterator.next())) {
          listIterator.remove();
        }
      }
      if (entry.getValue().isEmpty()) {
        iterator.remove();
      }
    }
  }

  private static @Nullable String getNodeModulesPath(@NotNull Angular2MetadataElement element) {
    return stripNodeModulesPath(doIfNotNull(element.getContainingFile().getOriginalFile().getVirtualFile(), VirtualFile::getPath));
  }

  private static @Nullable String stripNodeModulesPath(@Nullable String path) {
    int index = path != null ? path.lastIndexOf(NODE_MODULES_SEGMENT) : -1;
    return index >= 0 ? path.substring(0, index + 1) : null;
  }

  private static String renderFileName(@NotNull Angular2MetadataElement element) {
    String name = element.getContainingFile().getVirtualFile().getPath();
    int index = name.lastIndexOf(NODE_MODULES_SEGMENT);
    if (index > 1) {
      index = name.lastIndexOf("/", index - 1);
      if (index > 0) {
        return name.substring(index);
      }
    }
    return name;
  }
}
