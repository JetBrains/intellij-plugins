package org.angularjs.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class EmptyEventHandlerInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new XmlElementVisitor() {
      @Override
      public void visitXmlAttribute(XmlAttribute attribute) {
        if (AngularAttributesRegistry.isEventAttribute(attribute.getName(), attribute.getProject())) {
          final XmlAttributeValue value = attribute.getValueElement();
          if (value == null || value.getChildren().length == 0) {
            holder.registerProblem(attribute, "Empty event handler attribute", new CreateAttributeQuickFix());
          }
        }
      }
    };
  }

  private static class CreateAttributeQuickFix implements LocalQuickFix {
    @Nls
    @NotNull
    @Override
    public String getName() {
      return "Add attribute value";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return "AngularJS";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final XmlAttribute attribute = (XmlAttribute)descriptor.getPsiElement();
      attribute.setValue("");
      PsiNavigationSupport.getInstance().createNavigatable(project, attribute.getContainingFile().getVirtualFile(),
                                                           attribute.getValueElement().getTextRange()
                                                                    .getStartOffset() + 1).navigate(true);

    }
  }
}
