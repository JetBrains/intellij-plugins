package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhoneGapFacade {

  public static boolean isPhoneGapProject(@NotNull final Project project) {

    return CachedValuesManager.getManager(project).getCachedValue(project, new CachedValueProvider<Boolean>() {
      @Nullable
      @Override
      public Result<Boolean> compute() {
        PsiFile[] files = FilenameIndex.getFilesByName(project, "config.xml", GlobalSearchScope.allScope(project));

        PsiFile file = ContainerUtil.find(files, new Condition<PsiFile>() {
          @Override
          public boolean value(PsiFile file) {
            if (!(file instanceof XmlFile)) return false;

            XmlTag root = ((XmlFile)file).getRootTag();
            if (root == null) return false;

            return root.getName().equals("widget");
          }
        });

        return Result.create(file != null, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
      }
    });
  }
}
