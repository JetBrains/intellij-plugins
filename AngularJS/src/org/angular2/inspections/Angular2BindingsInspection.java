// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.MultiMap;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.inspections.quickfixes.RemoveAttributeQuickFix;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.angular2.codeInsight.Angular2Processor.isTemplateTag;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;

public class Angular2BindingsInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    AttributeInfo info = descriptor.getInfo();
    boolean templateTag = isTemplateTag(attribute.getParent().getName());
    if (info.type == TEMPLATE_BINDINGS) {
      if (templateTag) {
        return;
      }
      visitTemplateBindings(holder, attribute, Angular2TemplateBindings.get(attribute));
      return;
    }
    else if (info.type != EVENT
             && info.type != PROPERTY_BINDING
             && info.type != BANANA_BOX_BINDING) {
      return;
    }
    List<Angular2Directive> sourceDirectives = descriptor.getSourceDirectives();
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
    DeclarationProximity proximity;
    if (sourceDirectives == null) {
      if (templateTag) {
        proximity = DeclarationProximity.DOES_NOT_EXIST;
      }
      else {
        return;
      }
    }
    else {
      proximity = scope.getDeclarationsProximity(sourceDirectives);
    }
    switch (proximity) {
      case IN_SCOPE:
        return;
      case PUBLIC_MODULE_EXPORT:
        //TODO provide quick fixes to import modules
      case DOES_NOT_EXIST:
        String message;
        ProblemHighlightType severity;
        // TODO take into account 'CUSTOM_ELEMENTS_SCHEMA' and 'NO_ERRORS_SCHEMA' value of '@NgModule.schemas'
        severity = (info.type != EVENT || templateTag) && scope.isFullyResolved()
                   ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                   : ProblemHighlightType.WEAK_WARNING;
        switch (info.type) {
          case EVENT:
            if (templateTag) {
              message = "Event '" + info.name + "' is not emitted by any applicable directive on an embedded template.";
            }
            else {
              message = "Event '" + info.name + "' is not emitted by any applicable directives nor by '"
                        + attribute.getParent().getName() + "' element.";
            }
            break;
          case PROPERTY_BINDING:
            if (templateTag) {
              message =
                "Property '" + info.name + "' is not provided by any applicable directive on an embedded template.";
            }
            else {
              message =
                "Property '" + info.name + "' is not provided by any applicable directives nor by '" +
                attribute.getParent().getName() +
                "' element.";
            }
            break;
          case BANANA_BOX_BINDING:
            message = "Can't bind to '" + attribute.getName() + "' since it is not provided by any applicable directives.";
            break;
          default:
            return;
        }
        holder.registerProblem(attribute.getNameElement(), message, severity,
                               new RemoveAttributeQuickFix(attribute.getName()));
    }
  }

  private static void visitTemplateBindings(@NotNull ProblemsHolder holder,
                                            @NotNull XmlAttribute attribute,
                                            @NotNull Angular2TemplateBindings bindings) {
    MultiMap<String, Angular2Directive> input2DirectiveMap = new MultiMap<>();
    List<Angular2Directive> matched = new Angular2ApplicableDirectivesProvider(bindings).getMatched();
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
    switch (scope.getDeclarationsProximity(matched)) {
      case PUBLIC_MODULE_EXPORT:
        //TODO provide quick fixes to import modules
      case DOES_NOT_EXIST:
        holder.registerProblem(attribute.getNameElement(),
                               "No directive is matched on attribute '" + bindings.getTemplateName() + "'.",
                               ProblemHighlightType.WEAK_WARNING);
        return;
      default:
    }
    matched.forEach(
      dir -> dir.getInputs().forEach(
        input -> input2DirectiveMap.putValue(input.getName(), dir)));

    for (Angular2TemplateBinding binding : bindings.getBindings()) {
      if (!binding.keyIsVar() && binding.getExpression() != null) {
        DeclarationProximity proximity = scope.getDeclarationsProximity(
          input2DirectiveMap.get(binding.getKey()));
        switch (proximity) {
          case IN_SCOPE:
            break;
          case PUBLIC_MODULE_EXPORT:
            //TODO provide quick fixes to import modules
          case DOES_NOT_EXIST:
            PsiElement element = ObjectUtils.notNull(binding.getKeyElement(), attribute.getNameElement());
            holder.registerProblem(element, "Property '" + binding.getKey() +
                                            "' is not provided by any applicable directive on an embedded template.",
                                   scope.isFullyResolved()
                                   ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                   : ProblemHighlightType.WEAK_WARNING);
            break;
        }
      }
    }
  }
}
