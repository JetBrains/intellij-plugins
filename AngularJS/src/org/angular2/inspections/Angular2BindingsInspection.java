// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.*;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static org.angular2.codeInsight.Angular2Processor.isTemplateTag;

public class Angular2BindingsInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public List<ProblemDescriptor> processFile(@NotNull PsiFile file, @NotNull InspectionManager manager) {
    if (file.getLanguage().isKindOf(XMLLanguage.INSTANCE)) {
      return super.processFile(file, manager);
    }
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new XmlElementVisitor() {

      @Override
      public void visitXmlAttribute(XmlAttribute attribute) {
        Angular2AttributeDescriptor descriptor = ObjectUtils.tryCast(attribute.getDescriptor(),
                                                                     Angular2AttributeDescriptor.class);
        Angular2Directive[] sourceDirectives;
        if (descriptor == null
            || (sourceDirectives = descriptor.getSourceDirectives()) == null) {
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
    };
  }
}
