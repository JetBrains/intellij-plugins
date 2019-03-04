// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub;
import org.angular2.index.Angular2MetadataNodeModuleIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.lang.javascript.library.JSLibraryUtil.NODE_MODULES;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX;

public class Angular2MetadataReference extends Angular2MetadataElement<Angular2MetadataReferenceStub> {
  @NonNls private static final Logger LOG = Logger.getInstance(Angular2MetadataReference.class);

  private static final Set<String> ourReportedErrors = new HashSet<>();

  public Angular2MetadataReference(@NotNull Angular2MetadataReferenceStub element) {
    super(element);
  }

  @Nullable
  public Angular2MetadataElement resolve() {
    String moduleName = getStub().getModule();
    if (moduleName != null) {
      String elementName = getStub().getName();
      String sourceModuleName = getNodeModule().getName();
      Project project = getProject();
      return CachedValuesManager.getCachedValue(this, () ->
        CachedValueProvider.Result.create(resolveFromPackage(elementName, moduleName, project, sourceModuleName),
                                          PsiModificationTracker.MODIFICATION_COUNT));
    }
    else {
      return tryCast(getNodeModule().findMember(getStub().getName()), Angular2MetadataElement.class);
    }
  }

  private Angular2MetadataElement resolveFromPackage(@NotNull String elementName,
                                                     @NotNull String moduleName,
                                                     @NotNull Project project,
                                                     @Nullable String sourceModuleName) {
    if (moduleName.startsWith("./") || moduleName.startsWith("../")) {
      Angular2MetadataNodeModule module = doIfNotNull(loadRelativeFile(moduleName, METADATA_SUFFIX),
                                                      file -> PsiTreeUtil.getStubChildOfType(file, Angular2MetadataNodeModule.class));
      return module != null ? tryCast(module.findMember(elementName), Angular2MetadataElement.class) : null;
    }
    MultiMap<Angular2MetadataElement, Angular2MetadataNodeModule> candidates = new MultiMap<>();
    StubIndex.getInstance().processElements(
      Angular2MetadataNodeModuleIndex.KEY, moduleName, project,
      GlobalSearchScope.allScope(project), Angular2MetadataNodeModule.class,
      nodeModule -> {
        if (nodeModule.isValid()) {
          doIfNotNull(tryCast(nodeModule.findMember(elementName),
                              Angular2MetadataElement.class),
                      element -> {
                        candidates.putValue(element, nodeModule);
                        return null;
                      });
        }
        return true;
      });
    if (candidates.size() > 1) {
      List<Angular2MetadataElement> inPackageRoot = ContainerUtil.findAll(
        candidates.keySet(), c -> ContainerUtil.exists(candidates.get(c), Angular2MetadataNodeModule::isPackageTypingsRoot));
      if (inPackageRoot.size() != 1 && ourReportedErrors.add(moduleName + "/" + elementName)) {
        LOG.error("Ambiguous resolution for import '" + moduleName + "/" + elementName
                  + "' in module '" + sourceModuleName + "'; candidates: " +
                  StreamEx.of(inPackageRoot.size() > 1 ? inPackageRoot : candidates.values())
                    .map(Angular2MetadataReference::renderFileName).joining(", "));
      }
      if (!inPackageRoot.isEmpty()) {
        return ContainerUtil.getFirstItem(inPackageRoot);
      }
    }
    return ContainerUtil.getFirstItem(candidates.keySet());
  }

  static String renderFileName(@NotNull Angular2MetadataElement element) {
    String name = element.getContainingFile().getVirtualFile().getPath();
    int index = name.lastIndexOf(NODE_MODULES);
    if (index > 0) {
      return name.substring(index);
    }
    return name;
  }

  @Override
  public String toString() {
    String module = getStub().getModule();
    return (module == null ? "" : module + "#") + getStub().getName() + " <metadata reference>";
  }
}
