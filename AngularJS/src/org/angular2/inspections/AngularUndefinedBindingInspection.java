// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.inspections.quickfixes.Angular2FixesFactory;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE;
import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;
import static org.angular2.lang.Angular2Bundle.BUNDLE;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;

public class AngularUndefinedBindingInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    AttributeInfo info = descriptor.getInfo();
    boolean templateTag = isTemplateTag(attribute.getParent());
    if (info.type == TEMPLATE_BINDINGS) {
      if (templateTag) {
        return;
      }
      visitTemplateBindings(holder, attribute, Angular2TemplateBindings.get(attribute));
      return;
    }
    else if (info.type == REFERENCE || info.type == LET || info.type == I18N
             || (info.type == PROPERTY_BINDING && ((PropertyBindingInfo)info).bindingType != PropertyBindingType.PROPERTY)) {
      return;
    }
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
    DeclarationProximity proximity;
    if (descriptor.hasErrorSymbols()) {
      proximity = NOT_REACHABLE;
    }
    else if (descriptor.hasNonDirectiveSymbols()) {
      if (templateTag) {
        proximity = NOT_REACHABLE;
      }
      else {
        return;
      }
    }
    else {
      Set<Angular2Directive> matchedDirectives = new HashSet<>(new Angular2ApplicableDirectivesProvider(
        attribute.getParent()).getMatched());
      proximity = scope.getDeclarationsProximity(ContainerUtil.findAll(descriptor.getSourceDirectives(), matchedDirectives::contains));
    }
    if (proximity == IN_SCOPE) {
      return;
    }
    List<LocalQuickFix> quickFixes = new SmartList<>();
    if (proximity != NOT_REACHABLE) {
      Angular2FixesFactory.addUnresolvedDeclarationFixes(attribute, quickFixes);
    }
    quickFixes.add(new RemoveAttributeIntentionFix(attribute.getName()));
    ProblemHighlightType severity;
    // TODO take into account 'CUSTOM_ELEMENTS_SCHEMA' and 'NO_ERRORS_SCHEMA' value of '@NgModule.schemas'
    severity = scope.isFullyResolved()
               ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
               : ProblemHighlightType.WEAK_WARNING;
    @PropertyKey(resourceBundle = BUNDLE) final String messageKey;
    switch (info.type) {
      case EVENT:
        if (templateTag) {
          messageKey = "angular.inspection.undefined-binding.message.embedded.event-not-emitted";
        }
        else {
          messageKey = "angular.inspection.undefined-binding.message.event-not-emitted";
        }
        break;
      case PROPERTY_BINDING:
        if (templateTag) {
          messageKey = "angular.inspection.undefined-binding.message.embedded.property-not-provided";
        }
        else {
          messageKey = "angular.inspection.undefined-binding.message.property-not-provided";
        }
        break;
      case BANANA_BOX_BINDING:
        messageKey = "angular.inspection.undefined-binding.message.banana-box-binding-not-provided";
        break;
      case REGULAR:
        if (proximity == NOT_REACHABLE) {
          messageKey = "angular.inspection.undefined-binding.message.unknown-attribute";
        }
        else {
          messageKey = "angular.inspection.undefined-binding.message.attribute-directive-out-of-scope";
        }
        severity = ProblemHighlightType.WARNING;
        break;
      default:
        return;
    }
    // TODO register error on the symbols themselves
    holder.registerProblem(attribute.getNameElement(),
                           Angular2Bundle.message(messageKey, info.name, attribute.getParent().getName()),
                           severity,
                           quickFixes.toArray(LocalQuickFix.EMPTY_ARRAY));
  }

  private static void visitTemplateBindings(@NotNull ProblemsHolder holder,
                                            @NotNull XmlAttribute attribute,
                                            @NotNull Angular2TemplateBindings bindings) {
    List<Angular2Directive> matched = new Angular2ApplicableDirectivesProvider(bindings).getMatched();
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
    DeclarationProximity proximity = scope.getDeclarationsProximity(matched);
    if (proximity != IN_SCOPE) {
      List<LocalQuickFix> fixes = new SmartList<>();
      Angular2FixesFactory.addUnresolvedDeclarationFixes(bindings, fixes);
      holder.registerProblem(attribute.getNameElement(),
                             Angular2Bundle.message("angular.inspection.undefined-binding.message.embedded.no-directive-matched",
                                                    bindings.getTemplateName()),
                             ProblemHighlightType.WEAK_WARNING,
                             fixes.toArray(LocalQuickFix.EMPTY_ARRAY));
      return;
    }

    MultiMap<String, Angular2Directive> input2DirectiveMap = new MultiMap<>();
    matched.forEach(
      dir -> dir.getInputs().forEach(
        input -> input2DirectiveMap.putValue(input.getName(), dir)));

    for (Angular2TemplateBinding binding : bindings.getBindings()) {
      if (!binding.keyIsVar() && binding.getExpression() != null) {
        proximity = scope.getDeclarationsProximity(input2DirectiveMap.get(binding.getKey()));
        if (proximity != IN_SCOPE) {
          PsiElement element = ObjectUtils.notNull(binding.getKeyElement(), attribute.getNameElement());
          List<LocalQuickFix> fixes = new SmartList<>();
          Angular2FixesFactory.addUnresolvedDeclarationFixes(binding, fixes);
          holder.registerProblem(element,
                                 Angular2Bundle.message("angular.inspection.undefined-binding.message.embedded.property-not-provided",
                                                        binding.getKey()),
                                 scope.isFullyResolved() ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                                         : ProblemHighlightType.WEAK_WARNING,
                                 fixes.toArray(LocalQuickFix.EMPTY_ARRAY));
        }
      }
    }
  }
}
