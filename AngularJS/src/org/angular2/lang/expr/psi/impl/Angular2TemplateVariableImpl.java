// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.impl.JSVariableImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import org.angular2.lang.expr.parser.Angular2StubElementTypes;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.psi.util.CachedValuesManager.getCachedValue;

public class Angular2TemplateVariableImpl extends JSVariableImpl<JSVariableStub<JSVariable>, JSVariable> {

  public Angular2TemplateVariableImpl(ASTNode node) {
    super(node);
  }

  public Angular2TemplateVariableImpl(JSVariableStub<JSVariable> stub) {
    super(stub, Angular2StubElementTypes.TEMPLATE_VARIABLE);
  }

  @Override
  public @Nullable JSType calculateType() {
    Angular2TemplateBindings bindings = PsiTreeUtil.getParentOfType(this, Angular2TemplateBindings.class);
    Angular2TemplateBinding binding = PsiTreeUtil.getParentOfType(this, Angular2TemplateBinding.class);
    if (binding == null || binding.getName() == null || bindings == null) {
      return null;
    }
    JSType propertyType = null;
    String propertyName = binding.getName();
    if (propertyName != null) {
      JSType contextType = JSResolveUtil.getElementJSType(bindings);
      if (contextType != null) {
        JSRecordType.PropertySignature signature = contextType.asRecordType().findPropertySignature(propertyName);
        propertyType = signature != null ? signature.getJSType() : null;
      }

      if (propertyType == null || propertyType instanceof JSAnyType) {
        for (Angular2TemplateBinding candidate : bindings.getBindings()) {
          if (candidate != binding && !candidate.keyIsVar() && propertyName.equals(candidate.getKey())) {
            propertyType = JSResolveUtil.getExpressionJSType(candidate.getExpression());
            break;
          }
        }
      }
    }
    return propertyType;
  }

  @Override
  public @Nullable JSType getJSType() {
    return getCachedValue(this, () ->
      create(calculateType(), PsiModificationTracker.MODIFICATION_COUNT));
  }

  @Override
  public boolean isLocal() {
    return true;
  }

  @Override
  protected @NotNull JSAttributeList.AccessType calcAccessType() {
    return JSAttributeList.AccessType.PUBLIC;
  }

  @Override
  public boolean useTypesFromJSDoc() {
    return false;
  }
}
