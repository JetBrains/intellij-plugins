// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleExportStub;
import org.angular2.index.Angular2MetadataNodeModuleIndex;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX;

public class Angular2MetadataModuleExport extends Angular2MetadataElement<Angular2MetadataModuleExportStub> {

  @NonNls private static final Logger LOG = Logger.getInstance(Angular2MetadataModuleExport.class);
  private static final Set<String> ourReportedErrors = new HashSet<>();

  public Angular2MetadataModuleExport(@NotNull Angular2MetadataModuleExportStub element) {
    super(element);
  }

  public MetadataElement findExport(@Nullable String name) {
    String mappedName = getStub().getExportMappings().isEmpty() ? name : getStub().getExportMappings().get(name);
    return mappedName != null ? doIfNotNull(getExportNodeModule(), module -> module.findMember(mappedName))
                              : null;
  }

  @Override
  public String toString() {
    @NonNls
    StringBuilder result = new StringBuilder();
    result.append("export ");
    if (!getStub().getExportMappings().isEmpty()) {
      result.append("{");
      result.append(StreamEx.of(getStub().getExportMappings().entrySet())
                      .map(e -> e.getValue() + " as " + e.getKey()) //NON-NLS
                      .joining(", "));
      result.append("}");
    }
    else {
      result.append("*");
    }
    return result.append(" from ")
      .append(getStub().getFrom())
      .append(" <metadata module export>").toString();
  }

  @Nullable
  private Angular2MetadataNodeModule getExportNodeModule() {
    return CachedValuesManager.getCachedValue(this, () ->
      CachedValueProvider.Result.create(getExportNodeModuleNoCache(), PsiModificationTracker.MODIFICATION_COUNT));
  }

  @Nullable
  private Angular2MetadataNodeModule getExportNodeModuleNoCache() {
    String from = getStub().getFrom();
    if (from == null) {
      return null;
    }
    if (from.startsWith("./") || from.startsWith("../")) {
      return doIfNotNull(loadRelativeFile(from, METADATA_SUFFIX),
                         file -> PsiTreeUtil.getStubChildOfType(file, Angular2MetadataNodeModule.class));
    }
    List<Angular2MetadataNodeModule> candidates = new SmartList<>();
    StubIndex.getInstance().processElements(
      Angular2MetadataNodeModuleIndex.KEY, from, getProject(),
      GlobalSearchScope.allScope(getProject()), Angular2MetadataNodeModule.class,
      nodeModule -> {
        if (nodeModule.isValid()) {
          candidates.add(nodeModule);
        }
        return true;
      });
    if (candidates.size() > 1) {
      List<Angular2MetadataNodeModule> inPackageRoot = ContainerUtil.findAll(
        candidates, Angular2MetadataNodeModule::isPackageTypingsRoot);
      if (inPackageRoot.size() != 1 && ourReportedErrors.add(from)) {
        LOG.error("Ambiguous resolution for module '" + from + "' in module '"
                  + getNodeModule().getName() + "'; candidates: " +
                  StreamEx.of(inPackageRoot.size() > 1 ? inPackageRoot : candidates)
                    .map(Angular2MetadataReference::renderFileName).joining(", "));
      }
      if (!inPackageRoot.isEmpty()) {
        return ContainerUtil.getFirstItem(inPackageRoot);
      }
    }
    return ContainerUtil.getFirstItem(candidates);
  }
}
