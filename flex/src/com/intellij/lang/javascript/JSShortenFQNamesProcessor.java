// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateContext;
import com.intellij.codeInsight.template.impl.TemplateOptionalProcessor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.validation.ActionScriptUnusedImportsHelper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class JSShortenFQNamesProcessor implements TemplateOptionalProcessor {
  private static final Logger LOG = Logger.getInstance(JSShortenFQNamesProcessor.class);

  @Override
  public void processText(final Project project,
                          final Template template,
                          final Document document,
                          final RangeMarker templateRange,
                          final Editor editor) {
    if (!template.isToShortenLongNames()) return;

    try {
      final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
      psiDocumentManager.commitDocument(document);
      final PsiFile hostFile = PsiUtilBase.getPsiFileInEditor(editor, project);
      final PsiFile file = (hostFile != null && JavaScriptSupportLoader.isFlexMxmFile(hostFile))
                           ? InjectedLanguageUtil.findInjectedPsiNoCommit(hostFile, templateRange.getStartOffset())
                           : hostFile;
      if (file instanceof JSFile && file.getLanguage().isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
        final ActionScriptUnusedImportsHelper.Results unusedImportsResults = ActionScriptUnusedImportsHelper.getUnusedImports(file);
        for (final JSReferenceExpression reference : unusedImportsResults.fqnsToReplaceWithShortName) {
          final TextRange range = InjectedLanguageManager.getInstance(project).injectedToHost(file, reference.getTextRange());
          if (TextRange.create(templateRange).contains(range)) {
            final String shortName = StringUtil.getShortName(reference.getReferencedName());
            final String resolved = JSImportHandlingUtil.resolveTypeName(shortName, reference);

            // insert import statement if needed
            if (shortName.equals(resolved)) {
              final FormatFixer fixer = ImportUtils.insertImportStatements(reference, Collections.singletonList(reference.getText()));
              if (fixer != null) {
                fixer.fixFormat();
              }
            }

            // shorten FQN
            reference.replace(JSChangeUtil.createExpressionFromText(project, shortName).getPsi());
          }
        }

        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  @Override
  public String getOptionName() {
    return CodeInsightBundle.message("dialog.edit.template.checkbox.shorten.fq.names");
  }

  @Override
  public boolean isEnabled(final Template template) {
    return template.isToShortenLongNames();
  }

  @Override
  public boolean isVisible(@NotNull Template template, @NotNull TemplateContext context) {
    return false;
  }
}
