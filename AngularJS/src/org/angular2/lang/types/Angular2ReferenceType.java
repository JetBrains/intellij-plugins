// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.javascript.web.types.WebJSTypesUtil;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.psi.util.CachedValuesManager.getCachedValue;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;
import static org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF;
import static org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE;
import static org.angular2.lang.types.Angular2TypeUtils.*;

public class Angular2ReferenceType extends Angular2BaseType<Angular2HtmlAttrVariableImpl> {

  public static SearchScope getUseScope(Angular2HtmlAttrVariableImpl variable) {
    final JSClass
      clazz = Angular2ComponentLocator.findComponentClass(variable);
    LocalSearchScope localScope;
    if (clazz != null) {
      localScope = new LocalSearchScope(new PsiElement[]{clazz, variable.getContainingFile()});
    }
    else {
      localScope = new LocalSearchScope(variable.getContainingFile());
    }
    return GlobalSearchScope.filesScope(variable.getProject(), GlobalSearchScopeUtil.getLocalScopeFiles(localScope));
  }

  public Angular2ReferenceType(@NotNull Angular2HtmlAttrVariableImpl variable) {
    super(variable, Angular2HtmlAttrVariableImpl.class);
    assert variable.getKind() == Angular2HtmlAttrVariable.Kind.REFERENCE : variable;
  }

  protected Angular2ReferenceType(@NotNull JSTypeSource source) {
    super(source, Angular2HtmlAttrVariableImpl.class);
    assert getSourceElement().getKind() == Angular2HtmlAttrVariable.Kind.REFERENCE : getSourceElement();
  }

  @Override
  protected @Nullable String getTypeOfText() {
    return doIfNotNull(PsiTreeUtil.getContextOfType(getSourceElement(), XmlAttribute.class), XmlAttribute::getName);
  }

  @Override
  protected @NotNull JSType copyWithNewSource(@NotNull JSTypeSource source) {
    return new Angular2ReferenceType(source);
  }

  @Override
  protected @Nullable JSType resolveType(@NotNull JSTypeSubstitutionContext context) {
    Angular2HtmlReference reference = getReferenceDefinitionAttribute();
    if (reference == null) {
      return null;
    }
    XmlTag tag = reference.getParent();
    if (tag != null) {
      Angular2DeclarationsScope scope = new Angular2DeclarationsScope(reference);
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
                                     ? getTemplateRefType(Angular2ComponentLocator.findComponentClass(tag),
                                                          getNgTemplateTagContextType(tag))
                                     : WebJSTypesUtil.getHtmlElementClassType(createJSTypeSourceForXmlElement(tag), tag.getName()));
    }
    return null;
  }

  private @Nullable Angular2HtmlReference getReferenceDefinitionAttribute() {
    return (Angular2HtmlReference)PsiTreeUtil.findFirstParent(
      getSourceElement(), Angular2HtmlReference.class::isInstance);
  }

  private static @Nullable JSType getTemplateRefType(@Nullable PsiElement scope, @Nullable JSType contextType) {
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
                                   contextType != null ? contextType : JSAnyType.get(templateRefClass, true));
    });
  }
}
