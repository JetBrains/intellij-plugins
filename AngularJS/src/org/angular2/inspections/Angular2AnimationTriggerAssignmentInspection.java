// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class Angular2AnimationTriggerAssignmentInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    PropertyBindingInfo info = ObjectUtils.tryCast(descriptor.getInfo(), PropertyBindingInfo.class);
    if (info != null
        && info.bindingType == PropertyBindingType.ANIMATION
        && attribute.getName().startsWith("@")
        && attribute.getValueElement() != null
        && !StringUtil.notNullize(attribute.getValue()).isEmpty()) {
      holder.registerProblem(attribute.getValueElement(),
                             Angular2Bundle.message("angular.inspection.template.animation-trigger-assignment"),
                             new ConvertToPropertyBindingQuickFix(info.name),
                             new RemoveAttributeValueQuickFix());
    }
  }

  private static class ConvertToPropertyBindingQuickFix implements LocalQuickFix {

    private final String myAnimationTrigger;

    private ConvertToPropertyBindingQuickFix(@NotNull String animationTrigger) {
      myAnimationTrigger = animationTrigger;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
      return Angular2Bundle.message("angular.quickfix.template.bind-to-property.name", myAnimationTrigger);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return Angular2Bundle.message("angular.quickfix.family");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final XmlAttribute attribute = ObjectUtils.tryCast(descriptor.getPsiElement().getParent(), XmlAttribute.class);
      if (attribute != null) {
        attribute.setName("[@" + myAnimationTrigger + "]");
      }
    }
  }

  private static class RemoveAttributeValueQuickFix implements LocalQuickFix {

    @Nls
    @NotNull
    @Override
    public String getName() {
      return Angular2Bundle.message("angular.quickfix.template.remove-attribute-value.name");
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return Angular2Bundle.message("angular.quickfix.family");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final XmlAttribute attribute = ObjectUtils.tryCast(descriptor.getPsiElement().getParent(), XmlAttribute.class);
      if (attribute != null && attribute.getValueElement() != null) {
        attribute.deleteChildRange(attribute.getNameElement().getNextSibling(),
                                   attribute.getValueElement());
        FormatFixer.create(attribute, FormatFixer.Mode.Reformat).fixFormat();
      }
    }
  }
}
