// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.presentable.Capitalization;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThreeState;
import com.intellij.util.concurrency.ThreadingAssertions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FlexRefactoringUtil {
  private FlexRefactoringUtil() {
  }

  public static @Nullable PsiDirectory chooseOrCreateDirectoryForClass(final @NotNull Project project,
                                                                       final @Nullable Module module,
                                                                       final GlobalSearchScope scope,
                                                                       final @NotNull String packageName,
                                                                       final @Nullable String className,
                                                                       final @Nullable PsiDirectory baseDir,
                                                                       final ThreeState chooseFlag) {
    ThreadingAssertions.assertEventDispatchThread();

    if (className != null) {
      final String qName = StringUtil.getQualifiedName(packageName, className);
      PsiElement clazz = JSDialectSpecificHandlersFactory.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
        .findClassByQName(qName, scope);
      if (clazz != null) {
        String message = FlexBundle
          .message("item.already.exists",
                   new JSNamedElementPresenter(clazz, Capitalization.UpperCase).describeElementKind(),
                   qName);
        Messages.showErrorDialog(project, message, CommonBundle.getErrorTitle());
        return null;
      }
    }

    final Ref<@NlsContexts.DialogMessage String> error = new Ref<>();
    final Ref<PsiDirectory> result = new Ref<>();
    CommandProcessor.getInstance().executeCommand(project, () -> {
      try {
        result.set(PlatformPackageUtil.findOrCreateDirectoryForPackage(project, module, scope, packageName, baseDir, true, chooseFlag));
        if (result.isNull()) {
          error.set(""); // message already reported by PlatformPackageUtil
          return;
        }
        if (className != null) {
          error.set(checkCanCreateFile(result.get(), className));
        }
      }
      catch (IncorrectOperationException e) {
        error.set(e.getMessage());
      }
    }, CodeInsightBundle.message("create.directory.command"), null);

    if (!error.isNull()) {
      if (!error.get().isEmpty()) {
        Messages.showErrorDialog(project, error.get(), CommonBundle.getErrorTitle());
        return null;
      }
    }
    return result.get();
  }

  private static @Nullable @NlsContexts.DialogMessage String checkCanCreateFile(PsiDirectory directory, String className) {
    for (VirtualFile file : directory.getVirtualFile().getChildren()) {
      if (className.equals(file.getNameWithoutExtension()) &&
          (FileTypeManager.getInstance().isFileOfType(file, JavaScriptSupportLoader.JAVASCRIPT) ||
           FlexSupportLoader.isMxmlOrFxgFile(file))) {
        return FlexBundle.message("directory.already.contains.file", directory.getVirtualFile().getPresentableUrl(), file.getName());
      }
    }
    return null;
  }
}
