package org.jetbrains.vuejs.libraries.thymeleaf;

import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.thymeleaf.lang.support.utils.ThymeleafCommonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.context.VueContextProvider;
import org.jetbrains.vuejs.context.VueSimpleHtmlContextProvider;

import java.util.ArrayList;
import java.util.List;

public class VueThymeleafContextProvider implements VueContextProvider {

  private final ThreadLocal<Boolean> calculating = new ThreadLocal<>();

  @Override
  public boolean isVueContextEnabled(@NotNull PsiFile file) {
    return file instanceof XmlFile
           && ThymeleafCommonUtil.hasThymeleafLibrary(file.getProject())
           && calculating.get() != Boolean.TRUE
           && CachedValuesManager.getCachedValue(file, () -> {
      List<PsiFile> files;
      calculating.set(true);
      try {
        files = ThymeleafCommonUtil.findIncludedFiles((XmlFile)file);
      } catch (NoSuchMethodError error) {
        // Old thymeleaf plugin in version < 2020.1.1 - just return never changed.
        return CachedValueProvider.Result.create(false, ModificationTracker.NEVER_CHANGED);
      } finally {
        calculating.set(null);
      }
      for (PsiFile included : files) {
        if (included instanceof XmlFile && VueSimpleHtmlContextProvider.hasVueLibraryImport(included)) {
          return CachedValueProvider.Result.create(true, file, included);
        }
      }
      List<Object> dependencies = new ArrayList<>(files.size() + 2);
      dependencies.add(file);
      dependencies.add(VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
      dependencies.addAll(files);
      return CachedValueProvider.Result.create(false, dependencies);
    });
  }
}
