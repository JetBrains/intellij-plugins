// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.source.Angular2SourceDeclaration;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.*;

public class Angular2FixesFactory {

  public static void addUnresolvedDeclarationFixes(@NotNull PsiElement element, List<LocalQuickFix> fixes) {
    MultiMap<DeclarationProximity, Angular2Declaration> candidates = getCandidates(element);
    if (candidates.containsKey(IN_SCOPE)) {
      return;
    }
    if (!candidates.get(EXPORTED_BY_PUBLIC_MODULE).isEmpty()) {
      fixes.add(new AddNgModuleImportQuickFix(element, candidates.get(EXPORTED_BY_PUBLIC_MODULE)));
    }
    for (Angular2Declaration declaration : candidates.get(NOT_DECLARED_IN_ANY_MODULE)) {
      if (declaration instanceof Angular2SourceDeclaration) {
        fixes.add(new AddNgModuleDeclarationQuickFix(element, (Angular2SourceDeclaration)declaration));
      }
    }
    for (Angular2Declaration declaration : candidates.get(NOT_EXPORTED_BY_MODULE)) {
      if (declaration instanceof Angular2SourceDeclaration) {
        fixes.add(new ExportNgModuleDeclarationQuickFix(element, (Angular2SourceDeclaration)declaration));
      }
    }
  }

  static MultiMap<DeclarationProximity, Angular2Declaration> getCandidates(@NotNull PsiElement element) {
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(element);
    if (scope.getModule() == null || !scope.isInSource(scope.getModule())) {
      return MultiMap.empty();
    }
    Predicate<Angular2Declaration> filter = declaration -> true;
    Supplier<List<? extends Angular2Declaration>> provider;
    if (element instanceof XmlAttribute) {
      Angular2AttributeDescriptor attributeDescriptor = tryCast(((XmlAttribute)element).getDescriptor(),
                                                                Angular2AttributeDescriptor.class);
      if (attributeDescriptor == null) {
        return MultiMap.empty();
      }
      provider = new Angular2ApplicableDirectivesProvider(((XmlAttribute)element).getParent())::getMatched;
      if (attributeDescriptor.getInfo().type == Angular2AttributeType.REFERENCE) {
        String exportName = ((XmlAttribute)element).getValue();
        if (exportName == null || exportName.isEmpty()) {
          return MultiMap.empty();
        }
        filter = filter.and(declaration -> declaration instanceof Angular2Directive
                                           && ((Angular2Directive)declaration).getExportAsList().contains(exportName));
      }
    }
    else if (element instanceof XmlTag) {
      provider = new Angular2ApplicableDirectivesProvider((XmlTag)element, true)::getMatched;
    }
    else if (element instanceof Angular2TemplateBinding) {
      provider = new Angular2ApplicableDirectivesProvider((Angular2TemplateBindings)element.getParent())::getMatched;
    }
    else if (element instanceof Angular2TemplateBindings) {
      provider = new Angular2ApplicableDirectivesProvider((Angular2TemplateBindings)element)::getMatched;
    }
    else if (element instanceof Angular2PipeReferenceExpression) {
      String referencedName = ((Angular2PipeReferenceExpression)element).getReferenceName();
      if (referencedName == null || referencedName.isEmpty()) {
        return MultiMap.empty();
      }
      provider = () -> Angular2EntitiesProvider.findPipes(element.getProject(), referencedName);
    }
    else {
      throw new IllegalArgumentException(element.getClass().getName());
    }
    MultiMap<DeclarationProximity, Angular2Declaration> result = new MultiMap<>();
    StreamEx.of(provider.get())
      .filter(filter)
      .forEach(declaration -> result.putValue(
        scope.getDeclarationProximity(declaration), declaration));
    return result;
  }
}
