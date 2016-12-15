package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.modules.diagramm.JSModuleConnectionProvider;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

/**
 * @author Irina.Chernushina on 12/15/2016.
 */
public class AngularModulesProvider implements JSModuleConnectionProvider {
  @Override
  public Color getEdgeColor() {
    return JBColor.RED;
  }

  @Override
  public String getName() {
    return "AngularJS";
  }

  @Override
  public List<Link > getDependencies(@NotNull PsiFile file) {
    // todo debug
    //final SmartPointerManager spm = SmartPointerManager.getInstance(file.getProject());
    //return Collections.singletonList(
    //  new Link(spm.createSmartPsiElementPointer(file), spm.createSmartPsiElementPointer(file), file.getName()));
    return null;
  }
}
