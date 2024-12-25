// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.files.CfmlFileViewProvider;
import com.intellij.coldFusion.model.psi.CfmlImport;
import com.intellij.coldFusion.model.psi.stubs.CfmlIndex;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

final class CfmlTagNamesCompletionProvider extends CompletionProvider<CompletionParameters> {
  @Override
  public void addCompletions(final @NotNull CompletionParameters parameters,
                             final @NotNull ProcessingContext context,
                             final @NotNull CompletionResultSet result) {
    if (!(parameters.getOriginalFile().getViewProvider() instanceof CfmlFileViewProvider)) {
      return;
    }

    for (String s : CfmlUtil.getTagList(parameters.getPosition().getProject())) {
      result.addElement(LookupElementBuilder.create(s).withCaseSensitivity(false));
    }

    CfmlImport[] imports = PsiTreeUtil.getChildrenOfType(parameters.getOriginalFile(), CfmlImport.class);
    if (imports == null) {
      return;
    }
    for (CfmlImport cfmlImport : imports) {
      final String prefix = cfmlImport.getPrefix();
      if (prefix != null) {
        addCompletionsFromDirectory(result, parameters, cfmlImport.getImportString(), prefix);
      }
    }
  }

  private static void addCompletionsFromDirectory(CompletionResultSet result, CompletionParameters parameters, String libtag, String prefix) {
    final PsiFile originalFile = parameters.getOriginalFile();
    final VirtualFile folder = CfmlUtil.findFileByLibTag(originalFile, libtag);
    if (folder != null && folder.isDirectory()) {
      final Set<String> names = new HashSet<>(CfmlIndex.getInstance(originalFile.getProject()).getAllComponentsNames());
      names.retainAll(ContainerUtil.map(folder.getChildren(),
                                        virtualFile -> StringUtil.toLowerCase(FileUtilRt.getNameWithoutExtension(virtualFile.getName()))));
      for (String componentName : names) {
        result.addElement(LookupElementBuilder.create(prefix + ':' + componentName).withCaseSensitivity(false));
      }
    }
  }
}
