package org.angularjs;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: johnlindquist
 * Date: 4/8/13
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class AngularInspection extends HtmlLocalInspectionTool {
    @Override
    protected void checkAttribute(@NotNull XmlAttribute attribute, @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        super.checkAttribute(attribute, holder, isOnTheFly);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
