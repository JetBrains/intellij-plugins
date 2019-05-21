// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSVariableImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.psi.HintedReferenceHost;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ObjectUtils;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.html.parser.Angular2HtmlStubElementTypes;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlReferenceVariable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.psi.util.CachedValuesManager.getCachedValue;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;
import static org.angular2.codeInsight.template.Angular2TemplateScopesResolver.getHtmlElementClassType;
import static org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF;

public class Angular2HtmlReferenceVariableImpl extends JSVariableImpl<JSVariableStub<JSVariable>, JSVariable>
  implements Angular2HtmlReferenceVariable, HintedReferenceHost {

  @NonNls public static final String ANGULAR_CORE_PACKAGE = "@angular/core";

  public Angular2HtmlReferenceVariableImpl(ASTNode node) {
    super(node);
  }

  public Angular2HtmlReferenceVariableImpl(JSVariableStub<JSVariable> stub) {
    super(stub, Angular2HtmlStubElementTypes.REFERENCE_VARIABLE);
  }

  @Nullable
  @Override
  public JSType calculateType() {
    Angular2HtmlReference reference = getReferenceDefinitionAttribute();
    if (reference == null) {
      return null;
    }
    XmlTag tag = reference.getParent();
    if (tag != null) {
      Angular2DeclarationsScope scope = new Angular2DeclarationsScope(this);
      String exportName = reference.getValue();
      boolean hasExport = exportName != null && !exportName.isEmpty();
      return StreamEx.of(new Angular2ApplicableDirectivesProvider(tag).getMatched())
        .filter(directive -> scope.contains(directive)
                             && hasExport ? directive.getExportAsList().contains(exportName)
                                          : directive.isComponent())
        .map(directive -> directive.getTypeScriptClass())
        .nonNull()
        .map(TypeScriptClass::getJSType)
        .findFirst()
        .orElseGet(() -> hasExport ? null
                                   : isTemplateTag(tag)
                                     ? getTemplateRefType(getComponentClass())
                                     : getHtmlElementClassType(this, tag.getName()));
    }
    return null;
  }

  @Nullable
  @Override
  public JSType getJSType() {
    return getCachedValue(this, () ->
      create(calculateType(), PsiModificationTracker.MODIFICATION_COUNT));
  }

  @Nullable
  private static JSType getTemplateRefType(@Nullable PsiElement scope) {
    return scope == null ? null : doIfNotNull(getCachedValue(scope, () -> {
      for (PsiElement module : JSFileReferencesUtil.resolveModuleReference(scope, ANGULAR_CORE_PACKAGE)) {
        if (!(module instanceof JSElement)) continue;
        TypeScriptClass templateRefClass = ObjectUtils.tryCast(
          JSResolveResult.resolve(
            ES6PsiUtil.resolveSymbolInModule(TEMPLATE_REF, scope, (JSElement)module)),
          TypeScriptClass.class);
        if (templateRefClass != null
            && templateRefClass.getTypeParameters().length == 1) {
          return create(templateRefClass, PsiModificationTracker.MODIFICATION_COUNT);
        }
      }
      return create(null, PsiModificationTracker.MODIFICATION_COUNT);
    }), templateRefClass -> {
      JSType baseType = templateRefClass.getJSType();
      return new JSGenericTypeImpl(baseType.getSource(), baseType,
                                   JSAnyType.get(templateRefClass, true));
    });
  }

  @Nullable
  private PsiElement getComponentClass() {
    return Angular2IndexingHandler.findComponentClass(this);
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public boolean isExported() {
    return true;
  }

  @NotNull
  @Override
  public SearchScope getUseScope() {
    final JSClass clazz = Angular2IndexingHandler.findComponentClass(this);
    LocalSearchScope localScope;
    if (clazz != null) {
      localScope = new LocalSearchScope(new PsiElement[]{clazz, this.getContainingFile()});
    }
    else {
      localScope = new LocalSearchScope(this.getContainingFile());
    }
    return GlobalSearchScope.filesScope(getProject(), GlobalSearchScopeUtil.getLocalScopeFiles(localScope));
  }

  @Override
  public void delete() throws IncorrectOperationException {
    Angular2HtmlReference ref = getReferenceDefinitionAttribute();
    if (ref != null) {
      ref.delete();
    }
    else {
      super.delete();
    }
  }

  @NotNull
  @Override
  protected JSAttributeList.AccessType calcAccessType() {
    return JSAttributeList.AccessType.PUBLIC;
  }

  @Override
  public boolean useTypesFromJSDoc() {
    return false;
  }

  @Nullable
  private Angular2HtmlReference getReferenceDefinitionAttribute() {
    return (Angular2HtmlReference)PsiTreeUtil.findFirstParent(
      this, Angular2HtmlReference.class::isInstance);
  }

  @NotNull
  @Override
  public PsiReference[] getReferences(@NotNull PsiReferenceService.Hints hints) {
    return super.getReferences();
  }

  @Override
  public boolean shouldAskParentForReferences(@NotNull PsiReferenceService.Hints hints) {
    return false;
  }
}
