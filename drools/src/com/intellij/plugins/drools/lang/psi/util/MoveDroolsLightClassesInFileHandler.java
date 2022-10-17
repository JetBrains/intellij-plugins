package com.intellij.plugins.drools.lang.psi.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveAllClassesInFileHandler;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MoveDroolsLightClassesInFileHandler extends MoveAllClassesInFileHandler {

  @Override
  public void processMoveAllClassesInFile(@NotNull Map<PsiClass, Boolean> allClasses, @NotNull PsiClass psiClass, PsiElement... elementsToMove) {
    if (psiClass instanceof DroolsLightClass) {
      final PsiClassOwner containingFile = (PsiClassOwner)psiClass.getContainingFile();
      final DroolsLightClass[] classes =
        ContainerUtil.map2Array(containingFile.getClasses(), DroolsLightClass.class, aClass -> new DroolsLightClass(aClass));
      boolean all = true;
      for (DroolsLightClass aClass : classes) {
        if (ArrayUtil.find(elementsToMove, aClass) == -1) {
          all = false;
          break;
        }
      }
      for (DroolsLightClass aClass : classes) {
        allClasses.put(aClass, all);
      }
    }
  }
}
