/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
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
package org.angularjs;

import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.FindResult;
import com.intellij.find.FindUtil;
import com.intellij.find.findInProject.FindInProjectManager;
import com.intellij.find.impl.FindInProjectUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.util.AdapterProcessor;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GotoAngularAction extends GotoActionBase {
    public GotoAngularAction() {
        getTemplatePresentation().setText(IdeBundle.message("goto.inspection.action.text"));
    }

    @Override
    protected void gotoActionPerformed(final AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) return;

        PsiDocumentManager.getInstance(project).commitAllDocuments();

        final DataContext dataContext = e.getDataContext();
        final PsiElement psiElement = LangDataKeys.PSI_ELEMENT.getData(dataContext);
        final PsiFile psiFile = LangDataKeys.PSI_FILE.getData(dataContext);
        final VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);

        FindManager findManager = FindManager.getInstance(project);
        FindModel findModel = (FindModel) findManager.getFindInFileModel().clone();
        findModel.setRegularExpressions(true);
        findModel.setStringToFind("\\.controller\\((.*),");
        FindInProjectUtil.setDirectoryName(findModel, dataContext);
        final PsiDirectory psiDirectory = FindInProjectUtil.getPsiDirectory(findModel, project);


        CommonProcessors.CollectProcessor<Usage> collectProcessor = new CommonProcessors.CollectProcessor<Usage>() {
            @Override
            protected boolean accept(Usage usage) {
                return super.accept(usage);    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public Collection<Usage> getResults() {
                return super.getResults();    //To change body of overridden methods use File | Settings | File Templates.
            }
        };
        FindInProjectUtil.findUsages(findModel, psiDirectory, project,
                true, new AdapterProcessor<UsageInfo, Usage>(collectProcessor, UsageInfo2UsageAdapter.CONVERTER));

        Collection<Usage> results = collectProcessor.getResults();

        for (Usage result : results) {
            /*Document document = ((UsageInfo2UsageAdapter) result).getDocument();
            Segment segment = ((UsageInfo2UsageAdapter) result).getUsageInfo().getSegment();
            TextRange range = new TextRange(segment.getStartOffset(), segment.getEndOffset());
            String text = document.getText(range);
            System.out.println(text);*/

            TextChunk[] chunks = ((UsageInfo2UsageAdapter) result).getText();
            for (int i = 0; i < chunks.length; i++) {
                TextChunk chunk = chunks[i];
                if (chunk.getText().equals("controller")) {
                    System.out.println(chunks[i + 2]);
                }
            }
        }


        final GotoAngularModel model = new GotoAngularModel(project);
        showNavigationPopup(e, model, new GotoActionBase.GotoActionCallback<Object>() {
            @Override
            protected ChooseByNameFilter<Object> createFilter(@NotNull ChooseByNamePopup popup) {
                popup.setSearchInAnyPlace(true);
                return super.createFilter(popup);
            }

            @Override
            public void elementChosen(ChooseByNamePopup popup, final Object element) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                    }


                });
            }
        });
    }
}
