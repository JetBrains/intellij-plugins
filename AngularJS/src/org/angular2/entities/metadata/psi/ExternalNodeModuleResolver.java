// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.diagnostic.Logger;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.intellij.lang.javascript.library.JSLibraryUtil.NODE_MODULES;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX;

public class ExternalNodeModuleResolver {
  @NonNls private static final Logger LOG = Logger.getInstance(ExternalNodeModuleResolver.class);

  private static final Set<String> ourReportedErrors = new HashSet<>();

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
    MultiMap<Angular2MetadataElement, Angular2MetadataNodeModule> candidates = new MultiMap<>();
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
      List<Angular2MetadataElement> inPackageRoot = ContainerUtil.findAll(
        candidates.keySet(), c -> ContainerUtil.exists(candidates.get(c), Angular2MetadataNodeModule::isPackageTypingsRoot));
      if (inPackageRoot.size() != 1 && ourReportedErrors.add(myModuleName + "/" + myMemberName)) {
        LOG.error("Ambiguous resolution for import '" + myModuleName + "/" + myMemberName
                  + "' in module '" + mySource.getNodeModule().getName() + "'; candidates: " +
                  StreamEx.of(inPackageRoot.size() > 1 ? inPackageRoot : candidates.values())
                    .map(ExternalNodeModuleResolver::renderFileName).joining(", "));
      }
      if (!inPackageRoot.isEmpty()) {
        return ContainerUtil.getFirstItem(inPackageRoot);
      }
    }
    return ContainerUtil.getFirstItem(candidates.keySet());
  }

  private static String renderFileName(@NotNull Angular2MetadataElement element) {
    String name = element.getContainingFile().getVirtualFile().getPath();
    int index = name.lastIndexOf(NODE_MODULES);
    if (index > 0) {
      index = name.lastIndexOf("/", index);
      if (index > 0) {
        return name.substring(index);
      }
    }
    return name;
  }
}
