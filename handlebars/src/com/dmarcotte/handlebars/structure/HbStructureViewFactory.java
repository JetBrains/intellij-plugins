package com.dmarcotte.handlebars.structure;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.file.HbFileType;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.impl.StructureViewComposite;
import com.intellij.ide.structureView.impl.TemplateLanguageStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbStructureViewFactory implements PsiStructureViewFactory {
  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TemplateLanguageStructureViewBuilder(psiFile) {
      @Override
      protected StructureViewComposite.StructureViewDescriptor createMainView(FileEditor fileEditor, PsiFile mainFile) {
        if (!psiFile.isValid()) return null;

        final StructureViewBuilder builder = new TreeBasedStructureViewBuilder() {
          @NotNull
          @Override
          public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
            return new HbStructureViewModel((HbPsiFile)psiFile, editor);
          }
        };

        StructureView structureView = builder.createStructureView(fileEditor, psiFile.getProject());

        return new StructureViewComposite.StructureViewDescriptor(
          HbLanguage.INSTANCE.getDisplayName(), structureView, HbFileType.INSTANCE.getIcon());
      }
    };
  }
}
