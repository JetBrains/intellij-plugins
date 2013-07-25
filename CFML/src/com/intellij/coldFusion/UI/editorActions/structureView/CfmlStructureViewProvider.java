package com.intellij.coldFusion.UI.editorActions.structureView;

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

/**
 * Created by Lera Nikolaenko
 * Date: 20.01.2009
 */
public class CfmlStructureViewProvider implements PsiStructureViewFactory {
  @Override
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TemplateLanguageStructureViewBuilder(psiFile) {
      @Override
      protected StructureViewComposite.StructureViewDescriptor createMainView(final FileEditor fileEditor, final PsiFile mainFile) {
        StructureView mainView = new TreeBasedStructureViewBuilder() {
          @Override
          @NotNull
          public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
            return new CfmlStructureViewModel(psiFile);
          }
        }.createStructureView(fileEditor, mainFile.getProject());
        return new StructureViewComposite.StructureViewDescriptor("CFML View", mainView, mainFile.getFileType().getIcon());
      }
    };
  }
}
