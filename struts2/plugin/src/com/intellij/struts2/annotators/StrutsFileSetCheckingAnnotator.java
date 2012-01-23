/*
 * Copyright 2011 The authors
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
import com.intellij.ide.DataManager;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
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
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Set;

/**
 * Checks if <code>struts.xml</code> is registered in any of the file sets in the current module.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFileSetCheckingAnnotator implements Annotator {

  public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder holder) {
    if (psiElement instanceof JspFile) {
      return;
    }

    if (!(psiElement instanceof XmlFile)) {
      return;
    }

    final Module module = ModuleUtil.findModuleForPsiElement(psiElement);
    if (module == null) {
      return;
    }

    // do not run when facet not enabled
    if (StrutsFacet.getInstance(module) == null) {
      return;
    }

    final XmlFile xmlFile = (XmlFile) psiElement;
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
    final Annotation annotation =
        holder.createWarningAnnotation(xmlFile,
                                       fileSetAvailable ?
                                           StrutsBundle.message("annotators.fileset.file.not.registered") :
                                           StrutsBundle.message("annotators.fileset.no.file.sets"));
    annotation.setFileLevelAnnotation(true);

    if (fileSetAvailable) {
      final AddToFileSetFix addToFileSetFix = new AddToFileSetFix(xmlFile.getName());
      annotation.registerFix(addToFileSetFix);
    } else {
      annotation.registerFix(new IntentionAction() {
        @NotNull
        public String getText() {
          return StrutsBundle.message("annotators.fileset.edit.facet.settings");
        }

        @NotNull
        public String getFamilyName() {
          return StrutsBundle.message("intentions.family.name");
        }

        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile psiFile) {
          return true;
        }

        public void invoke(@NotNull final Project project,
                           final Editor editor,
                           final PsiFile psiFile) throws IncorrectOperationException {
          final StrutsFacet strutsFacet = StrutsFacet.getInstance(module);
          assert strutsFacet != null;
          ModulesConfigurator.showFacetSettingsDialog(strutsFacet, null);
        }

        public boolean startInWriteAction() {
          return false;
        }
      });

    }
  }


  /**
   * Adds the current struts.xml file to an existing file set.
   */
  private static class AddToFileSetFix extends BaseIntentionAction implements Iconable {

    private AddToFileSetFix(final String filename) {
      setText(StrutsBundle.message("annotators.fileset.fix.add.to.fileset", filename));
    }

    @NotNull
    public String getFamilyName() {
      return StrutsBundle.message("intentions.family.name");
    }

    @Override
    public Icon getIcon(final int flags) {
      return StrutsIcons.ACTION;
    }

    public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
      return true;
    }

    public void invoke(@NotNull final Project project,
                       final Editor editor,
                       final PsiFile file) throws IncorrectOperationException {
      final StrutsFacet strutsFacet = StrutsFacet.getInstance(file);
      assert strutsFacet != null;

      final Set<StrutsFileSet> strutsFileSets = strutsFacet.getConfiguration().getFileSets();
      final BaseListPopupStep<StrutsFileSet> step =
          new BaseListPopupStep<StrutsFileSet>(StrutsBundle.message("annotators.fileset.fix.choose.fileset"),
                                               new ArrayList<StrutsFileSet>(strutsFileSets)) {

            public Icon getIconFor(final StrutsFileSet aValue) {
              return StrutsIcons.STRUTS_CONFIG_FILE;
            }

            public PopupStep onChosen(final StrutsFileSet selectedValue, final boolean finalChoice) {
              selectedValue.addFile(file.getVirtualFile());
              ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                  ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true);
                }
              });

              // re-highlight (remove annotation)
              DaemonCodeAnalyzer.getInstance(project).restart();

              return super.onChosen(selectedValue, finalChoice);
            }
          };
      JBPopupFactory.getInstance()
                    .createListPopup(step)
                    .showInBestPositionFor(DataManager.getInstance().getDataContext(editor.getComponent()));
    }
  }

}