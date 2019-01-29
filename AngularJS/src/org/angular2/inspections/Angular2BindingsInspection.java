// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import static org.angular2.codeInsight.Angular2Processor.isTemplateTag;

public class Angular2BindingsInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    Angular2Directive[] sourceDirectives = descriptor.getSourceDirectives();
    if (sourceDirectives == null) {
      return;
    }
    AttributeInfo info = descriptor.getInfo();
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
    DeclarationProximity proximity = scope.getDeclarationsProximity(sourceDirectives);
    boolean templateTag = isTemplateTag(attribute.getParent().getName());
    switch (proximity) {
      case IN_SCOPE:
        return;
      case PUBLIC_MODULE_EXPORT:
        //TODO provide quick fixes to import modules
      case DOES_NOT_EXIST:
        String message;
        ProblemHighlightType severity;
        // TODO take into account 'CUSTOM_ELEMENTS_SCHEMA' and 'NO_ERRORS_SCHEMA' value of '@NgModule.schemas'
        severity = (info.type != Angular2AttributeType.EVENT || templateTag) && scope.isFullyResolved()
                   ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                   : ProblemHighlightType.WEAK_WARNING;
        switch (info.type) {
          case EVENT:
            if (templateTag) {
              message = "Event '" + info.name + "' is not emitted by any applicable directive.";
            }
            else {
              message = "Event '" + info.name + "' is not emitted by any applicable directives nor by '"
                        + attribute.getParent().getName() + "' element.";
            }
            break;
          case PROPERTY_BINDING:
            if (templateTag) {
              message =
                "Property '" + info.name + "' is not provided by any applicable directive.";
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
          case TEMPLATE_BINDINGS:
            if (templateTag) {
              message = "Structural directive bindings are not allowed on embedded templates.";
            }
            else {
              message =
                "Can't resolve '" + info.name + "' structural directive.";
            }
            break;
          default:
            return;
        }
        holder.registerProblem(attribute.getNameElement(), message, severity);
    }
  }
}
