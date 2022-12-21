// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2MetadataReference extends Angular2MetadataElement<Angular2MetadataReferenceStub> {

  public Angular2MetadataReference(@NotNull Angular2MetadataReferenceStub element) {
    super(element);
  }

  public @Nullable Angular2MetadataElement resolve() {
    String moduleName = getStub().getModule();
    if (moduleName != null) {
      String elementName = getStub().getName();
      return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(
        new ExternalNodeModuleResolver(this, moduleName, elementName).resolve(),
        PsiModificationTracker.MODIFICATION_COUNT));
    }
    else {
      return doIfNotNull(getNodeModule(), nodeModule ->
        tryCast(nodeModule.findMember(getStub().getName()), Angular2MetadataElement.class));
    }
  }

  @Override
  public String toString() {
    String module = getStub().getModule();
    String memberName = getStub().getMemberName();
    return (memberName == null ? "" : memberName + ": ")
           + (module == null ? "" : module + "#")
           + getStub().getName() + " <metadata reference>";
  }
}
