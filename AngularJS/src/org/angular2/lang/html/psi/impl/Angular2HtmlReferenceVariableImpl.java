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
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
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

import static com.intellij.lang.javascript.psi.types.JSNamedTypeFactory.createExplicitlyDeclaredType;
import static com.intellij.lang.javascript.psi.types.TypeScriptTypeParser.buildTypeFromClass;
import static org.angular2.codeInsight.Angular2Processor.getHtmlElementClassType;
import static org.angular2.codeInsight.Angular2Processor.isTemplateTag;
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
  protected JSType doGetType() {
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
        .findFirst()
        .map(directive -> getClassInstanceType(directive.getTypeScriptClass()))
        .orElseGet(() -> hasExport ? null
                                   : isTemplateTag(tag.getName())
                                     ? getTemplateRefType(getComponentClass())
                                     : getHtmlElementClassType(this, tag.getName()));
    }
    return null;
  }

  @Nullable
  public static JSType getTemplateRefType(@Nullable PsiElement scope) {
    if (scope == null) {
      return null;
    }
    return CachedValuesManager.getCachedValue(scope, () -> {
      for (PsiElement module : JSFileReferencesUtil.getMostPriorityModules(
        scope, ANGULAR_CORE_PACKAGE, false)) {
        if (module instanceof JSElement) {
          ResolveResult resolved = ArrayUtil.getFirstElement(
            ES6PsiUtil.resolveSymbolInModule(TEMPLATE_REF, scope, (JSElement)module));
          if (resolved != null && resolved.isValidResult() && resolved.getElement() instanceof TypeScriptClass) {
            TypeScriptClass templateRefClass = (TypeScriptClass)resolved.getElement();
            JSType baseType = getClassInstanceType(templateRefClass);
            if (baseType != null
                && templateRefClass.getTypeParameterList() != null
                && templateRefClass.getTypeParameterList().getTypeParameters().length == 1) {
              return CachedValueProvider.Result.create(
                new JSGenericTypeImpl(baseType.getSource(), baseType,
                                      JSAnyType.get(templateRefClass, true)),
                PsiModificationTracker.MODIFICATION_COUNT);
            }
          }
        }
      }
      return CachedValueProvider.Result.create(null, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  private static JSType getClassInstanceType(@Nullable TypeScriptClass clazz) {
    return clazz == null ? null
                         : clazz.getQualifiedName() != null
                           ? createExplicitlyDeclaredType(clazz.getQualifiedName(), clazz)
                           : buildTypeFromClass(clazz, false);
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
  protected boolean useTypesFromJSDoc() {
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
