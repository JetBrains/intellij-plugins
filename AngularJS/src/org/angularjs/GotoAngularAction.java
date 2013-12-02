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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Segment;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.FindUsagesProcessPresentation;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.util.AdapterProcessor;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GotoAngularAction extends GotoActionBase {
  private static final int MODULE_METHODS = 0;
  private static final int CTRL_CONVENTIONS = 1;
  private static final int NG_CONTROLLER = 2;

  public GotoAngularAction() {
    getTemplatePresentation().setText(IdeBundle.message("goto.inspection.action.text"));
  }

  @Override
  protected void gotoActionPerformed(final AnActionEvent e) {
    final Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) return;

    PsiDocumentManager.getInstance(project).commitAllDocuments();

    final DataContext dataContext = e.getDataContext();

    final FindManager findManager = FindManager.getInstance(project);
    final FindModel findModel = (FindModel)findManager.getFindInFileModel().clone();


    final List<AngularItem> validResults = new ArrayList<AngularItem>();

    findModel.setRegularExpressions(true);
    findModel.setFileFilter("*.js, *.html");

    findModel
      .setStringToFind("\\.(controller|filter|service|factory|module|value|constant|directive|provider)\\(\\s*(\"|')([^(\"|')]+)(\"|')");
    findModel.setStringToReplace("$3");
    final Collection<Usage> moduleMethodUsages = getAngularUsages(project, dataContext, findModel);
    List<AngularItem> moduleMethodResults = getValidResults(project, findModel, moduleMethodUsages, MODULE_METHODS);
    validResults.addAll(moduleMethodResults);

    findModel.setStringToFind("Ctrl\\(\\s*\\$scope");
    findModel.setStringToReplace("$0");
    final Collection<Usage> ctrlByConventionUsages = getAngularUsages(project, dataContext, findModel);
    List<AngularItem> ctrlByConventionResults = getValidResults(project, findModel, ctrlByConventionUsages, CTRL_CONVENTIONS);
    validResults.addAll(ctrlByConventionResults);


    findModel.setStringToFind("ng\\-controller\\=\"([^(\"]+)\"");
    findModel.setStringToReplace("$1");
    final Collection<Usage> ngControllerUsages = getAngularUsages(project, dataContext, findModel);
    List<AngularItem> ngControllerResults = getValidResults(project, findModel, ngControllerUsages, NG_CONTROLLER);
    validResults.addAll(ngControllerResults);


    final GotoAngularModel model = new GotoAngularModel(project, validResults);
    showNavigationPopup(e, model, new GotoActionBase.GotoActionCallback<Object>() {
      @Override
      protected ChooseByNameFilter<Object> createFilter(@NotNull ChooseByNamePopup popup) {
        popup.setSearchInAnyPlace(true);
        popup.setShowListForEmptyPattern(true);
        popup.setMaximumListSizeLimit(255);
        return super.createFilter(popup);
      }

      @Override
      public void elementChosen(ChooseByNamePopup popup, final Object element) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            PsiElement psi = ((AngularItem)element).getElement();
            NavigationUtil.activateFileWithPsiElement(psi.getNavigationElement());
          }
        });
      }
    });
  }

  private static List<AngularItem> getValidResults(final Project project,
                                                   final FindModel findModel,
                                                   final Collection<Usage> usages,
                                                   final int type) {
    final List<AngularItem> validResults = new ArrayList<AngularItem>();

    //todo: needs code review. There must be a better way to do this
    Runnable runnable = new Runnable() {
      public void run() {
        for (final Usage result : usages) {

          final UsageInfo2UsageAdapter usage = (UsageInfo2UsageAdapter)result;
          //avoid angular source files. Is there a better way to do this?
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
                String s = foundString.toString();
                String regExMatch = FindManager.getInstance(project).getStringToReplace(s, findModel, textOffset, document.getText());
                System.out.println(regExMatch);
                PsiElement element =
                  PsiUtilCore.getElementAtOffset(((UsageInfo2UsageAdapter)result).getUsageInfo().getFile(), textOffset + 1);
                String elementText = element.getText();
                System.out.println(elementText + ": " + regExMatch + " - " + s);
                //hack to block weird css matches (I have no idea how many edge cases I'll have :/ )
                //                                if(regExMatch.length() > 20) return true;

                switch (type) {
                  case CTRL_CONVENTIONS:
                    validResults.add(new AngularItem(s, elementText, result, element, "controller"));
                    break;

                  case MODULE_METHODS:
                    validResults.add(new AngularItem(s, regExMatch, result, element, element.getText()));
                    break;

                  case NG_CONTROLLER:
                    validResults.add(new AngularItem(s, regExMatch, result, element, "ng-controller"));
                    break;
                }

                return true;
              }
              catch (FindManager.MalformedReplacementStringException e1) {
                e1.printStackTrace();
              }

              return false;
            }
          });
        }
      }
    };

    ApplicationManager.getApplication().runReadAction(runnable);
    return validResults;
  }

  private static Collection<Usage> getAngularUsages(Project project, DataContext dataContext, FindModel findModel) {

    FindInProjectUtil.setDirectoryName(findModel, dataContext);

    CommonProcessors.CollectProcessor<Usage> collectProcessor = new CommonProcessors.CollectProcessor<Usage>();

    PsiDirectory directory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
    FindInProjectUtil.findUsages(findModel, directory, project,
                                 true, new AdapterProcessor<UsageInfo, Usage>(collectProcessor, UsageInfo2UsageAdapter.CONVERTER),
                                 new FindUsagesProcessPresentation());


    return collectProcessor.getResults();
  }
}
