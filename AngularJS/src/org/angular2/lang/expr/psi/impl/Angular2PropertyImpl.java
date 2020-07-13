// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSPropertyStub;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class Angular2PropertyImpl extends JSPropertyImpl {

  @NonNls private static final Key<ParameterizedCachedValue<PsiReference[], Angular2PropertyImpl>> REFERENCES_KEY =
    new Key<>("ng.property.references");
  private static final ParameterizedCachedValueProvider<PsiReference[], Angular2PropertyImpl> REFERENCES_PROVIDER =
    param -> CachedValueProvider.Result.create(param.createRefs(), param, PsiModificationTracker.MODIFICATION_COUNT);

  public Angular2PropertyImpl(ASTNode node) {
    super(node);
  }

  public Angular2PropertyImpl(JSPropertyStub stub) {
    super(stub);
  }

  @Override
  public PsiReference getReference() {
    return null;
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    return CachedValuesManager.getManager(getProject()).getParameterizedCachedValue(this, REFERENCES_KEY, REFERENCES_PROVIDER, false, this);
  }

  private PsiReference[] createRefs() {
    final JSElementIndexingData indexingData = getIndexingData();
    if (indexingData != null && ContainerUtil.isEmpty(indexingData.getImplicitElements())) {
      return PsiReference.EMPTY_ARRAY;
    }
    else if (getNameIdentifier() != null) {
      PsiReference[] result = ReferenceProvidersRegistry.getReferencesFromProviders(this);
      if (result.length > 0) {
        return result;
      }
    }
    final PsiReference propertyNameReference = getPropertyNameReference();
    return propertyNameReference != null
           ? new PsiReference[]{propertyNameReference}
           : PsiReference.EMPTY_ARRAY;
  }
}
