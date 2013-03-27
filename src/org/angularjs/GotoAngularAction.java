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

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
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
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.util.AdapterProcessor;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

        final FindManager findManager = FindManager.getInstance(project);
        final FindModel findModel = (FindModel) findManager.getFindInFileModel().clone();
        findModel.setRegularExpressions(true);
        findModel.setStringToFind("\\.(controller|filter|service|factory|module|value|constant|directive)\\(\\s*(\"|')(.*)(\"|')");
        findModel.setStringToReplace("$3");
        FindInProjectUtil.setDirectoryName(findModel, dataContext);

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

        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        FindInProjectUtil.findUsages(findModel, directory, project,
                true, new AdapterProcessor<UsageInfo, Usage>(collectProcessor, UsageInfo2UsageAdapter.CONVERTER));

        final Collection<Usage> results = collectProcessor.getResults();

        final Map<String, AngularItem> validResults = new HashMap<String, AngularItem>();

        //todo: needs code review. There must be a better way to do this
        Runnable runnable = new Runnable() {
            public void run() {
                for (final Usage result : results) {

                    final UsageInfo2UsageAdapter usage = (UsageInfo2UsageAdapter) result;
                    if (usage.getFile().getName().startsWith("angular")) continue;

                    usage.processRangeMarkers(new Processor<Segment>() {
                        @Override
                        public boolean process(Segment segment) {
                            try {
                                final int textOffset = segment.getStartOffset();

                                final int textEndOffset = segment.getEndOffset();
                                Document document = usage.getDocument();
                                CharSequence charsSequence = document.getCharsSequence();
                                final CharSequence foundString = charsSequence.subSequence(textOffset, textEndOffset);
                                String regExMatch = FindManager.getInstance(project).getStringToReplace(foundString.toString(), findModel, textOffset, document.getText());
                                System.out.println(regExMatch);
                                PsiElement element = PsiUtil.getElementAtOffset(((UsageInfo2UsageAdapter) result).getUsageInfo().getFile(), textOffset + 1);
                                System.out.println(element.getText() + ": " + regExMatch + " - " + foundString.toString());
                                //hack to block weird css matches (I have no idea how many edge cases I'll have :/ )
                                if(regExMatch.length() > 20) return true;
                                validResults.put(regExMatch, new AngularItem(regExMatch, result, element));

                                return true;
                            } catch (FindManager.MalformedReplacementStringException e1) {
                                e1.printStackTrace();
                            }

                            return false;
                        }
                    });
                }
            }
        };

        ApplicationManager.getApplication().runReadAction(runnable);


        final GotoAngularModel model = new GotoAngularModel(project, validResults);
        showNavigationPopup(e, model, new GotoActionBase.GotoActionCallback<Object>() {
            @Override
            protected ChooseByNameFilter<Object> createFilter(@NotNull ChooseByNamePopup popup) {
                popup.setSearchInAnyPlace(true);
                popup.setShowListForEmptyPattern(true);
                return super.createFilter(popup);
            }

            @Override
            public void elementChosen(ChooseByNamePopup popup, final Object element) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        PsiElement psi = ((AngularItem) element).getElement();
                        NavigationUtil.activateFileWithPsiElement(psi.getNavigationElement());
                    }
                });
            }
        });
    }
}
