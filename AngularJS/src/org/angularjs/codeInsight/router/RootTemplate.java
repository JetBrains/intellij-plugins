package org.angularjs.codeInsight.router;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;

import java.util.Set;

public class RootTemplate {
  private final SmartPsiElementPointer<PsiElement> myPointer;
  private final String myRelativeUrl;
  private final Template myTemplate;
  private final Set<VirtualFile> myModulesFiles;

  public RootTemplate(SmartPsiElementPointer<PsiElement> pointer,
                      String relativeUrl,
                      Template template,
                      Set<VirtualFile> modulesFiles) {
    myPointer = pointer;
    myRelativeUrl = relativeUrl;
    myTemplate = template;
    myModulesFiles = modulesFiles;
  }

  public SmartPsiElementPointer<PsiElement> getPointer() {
    return myPointer;
  }

  public String getRelativeUrl() {
    return myRelativeUrl;
  }

  public Template getTemplate() {
    return myTemplate;
  }

  public Set<VirtualFile> getModulesFiles() {
    return myModulesFiles;
  }
}
