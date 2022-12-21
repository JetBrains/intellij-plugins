// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import one.util.streamex.StreamEx;
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleExportStub;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2MetadataModuleExport extends Angular2MetadataElement<Angular2MetadataModuleExportStub> {

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

  private @Nullable Angular2MetadataNodeModule getExportNodeModule() {
    String from = getStub().getFrom();
    if (from == null) {
      return null;
    }
    return tryCast(
      CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(
        new ExternalNodeModuleResolver(this, from, null).resolve(),
        PsiModificationTracker.MODIFICATION_COUNT)),
      Angular2MetadataNodeModule.class);
  }
}
