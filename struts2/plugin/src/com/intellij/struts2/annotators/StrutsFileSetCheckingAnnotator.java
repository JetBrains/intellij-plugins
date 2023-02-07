/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.annotators;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.indexing.BuildableRootsChangeRescanningInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Set;

/**
 * Checks if {@code struts.xml} is registered in any of the file sets in the current module.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFileSetCheckingAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder holder) {
    if (!(psiElement instanceof XmlFile xmlFile)) {
      return;
    }

    if (psiElement instanceof JspFile) {
      return;
    }

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
    if (module == null) {
      return;
    }

    // do not run when facet not enabled
    if (StrutsFacet.getInstance(module) == null) {
      return;
    }

    final Project project = psiElement.getProject();

    final StrutsManager strutsManager = StrutsManager.getInstance(project);
    if (!strutsManager.isStruts2ConfigFile(xmlFile)) {
      return;
    }

    final VirtualFile currentVirtualFile = xmlFile.getVirtualFile();
    assert currentVirtualFile != null;

    final Set<StrutsFileSet> allConfigFileSets = strutsManager.getAllConfigFileSets(module);
    for (final StrutsFileSet configFileSet : allConfigFileSets) {
      if (configFileSet.hasFile(currentVirtualFile)) {
        return;
      }
    }

    final boolean fileSetAvailable = allConfigFileSets.size() != 0;

    IntentionAction fix;
    if (fileSetAvailable) {
      fix = new AddToFileSetFix(xmlFile.getName());
    }
    else {
      fix = new IntentionAction() {
        @Override
        @NotNull
        public String getText() {
          return StrutsBundle.message("annotators.fileset.edit.facet.settings");
        }

        @Override
        @NotNull
        public String getFamilyName() {
          return StrutsBundle.message("intentions.family.name");
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile psiFile) {
          return true;
        }

        @Override
        public void invoke(@NotNull final Project project,
                           final Editor editor,
                           final PsiFile psiFile) throws IncorrectOperationException {
          final StrutsFacet strutsFacet = StrutsFacet.getInstance(module);
          assert strutsFacet != null;
          ModulesConfigurator.showFacetSettingsDialog(strutsFacet, null);
        }

        @Override
        public boolean startInWriteAction() {
          return false;
        }
      };
    }
    holder.newAnnotation(HighlightSeverity.WARNING,
                                     fileSetAvailable ?
                                     StrutsBundle.message("annotators.fileset.file.not.registered") :
                                     StrutsBundle.message("annotators.fileset.no.file.sets"))
        .range(xmlFile)
    .fileLevel().withFix(fix).create();
  }


  /**
   * Adds the current struts.xml file to an existing file set.
   */
  private static final class AddToFileSetFix extends BaseIntentionAction implements Iconable {

    private AddToFileSetFix(final String filename) {
      setText(StrutsBundle.message("annotators.fileset.fix.add.to.fileset", filename));
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return StrutsBundle.message("intentions.family.name");
    }

    @Override
    public Icon getIcon(final int flags) {
      return Struts2Icons.Action;
    }

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
      return true;
    }

    @Override
    public void invoke(@NotNull final Project project,
                       final Editor editor,
                       final PsiFile file) throws IncorrectOperationException {
      final StrutsFacet strutsFacet = StrutsFacet.getInstance(file);
      assert strutsFacet != null;

      final Set<StrutsFileSet> strutsFileSets = strutsFacet.getConfiguration().getFileSets();
      final BaseListPopupStep<StrutsFileSet> step =
        new BaseListPopupStep<>(StrutsBundle.message("annotators.fileset.fix.choose.fileset"),
                                new ArrayList<>(strutsFileSets)) {

          @Override
          public Icon getIconFor(final StrutsFileSet aValue) {
            return StrutsIcons.STRUTS_CONFIG_FILE;
          }

          @Override
          public PopupStep onChosen(final StrutsFileSet selectedValue, final boolean finalChoice) {
            selectedValue.addFile(file.getVirtualFile());
            ApplicationManager.getApplication()
              .runWriteAction(() -> {
                Module module = strutsFacet.getModule();
                BuildableRootsChangeRescanningInfo info = BuildableRootsChangeRescanningInfo.newInstance().addModule(module);
                Module[] dependencies = ModuleRootManager.getInstance(module).getDependencies();
                for (Module dependency : dependencies) {
                  info.addModule(dependency);
                }
                ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), info);
              });

            // re-highlight (remove annotation)
            DaemonCodeAnalyzer.getInstance(project).restart();

            return super.onChosen(selectedValue, finalChoice);
          }
        };
      JBPopupFactory.getInstance()
        .createListPopup(step)
        .showInBestPositionFor(editor);
    }
  }
}
