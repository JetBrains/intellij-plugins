package org.angularjs;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.impl.FindInProjectUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Segment;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.FindUsagesProcessPresentation;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.util.AdapterProcessor;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;

import java.util.Collection;

public class AngularCompletionContributor extends CompletionContributor {
    @Override
    public void fillCompletionVariants(CompletionParameters parameters, final CompletionResultSet result) {
        result.addElement(LookupElementBuilder.create("huzzah"));

        final Project project = parameters.getOriginalFile().getProject();

        GlobalSearchScope scope = ProjectScope.getProjectScope(project);

        Collection<JSQualifiedNamedElement> scopes = JSResolveUtil.findElementsByName("$scope", project, scope);


        String s = "\\$scope\\.(\\w*)";

        final FindManager findManager = FindManager.getInstance(project);
        final FindModel findModel = (FindModel) findManager.getFindInFileModel().clone();


        findModel.setRegularExpressions(true);
        findModel.setFileFilter("*.js, *.html");

        findModel.setStringToFind(s);
        findModel.setStringToReplace("$1");

        CommonProcessors.CollectProcessor<Usage> collectProcessor = new CommonProcessors.CollectProcessor<Usage>();

        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        FindInProjectUtil.findUsages(findModel, directory, project,
                true, new AdapterProcessor<UsageInfo, Usage>(collectProcessor, UsageInfo2UsageAdapter.CONVERTER), new FindUsagesProcessPresentation());


        final Collection<Usage> usages = collectProcessor.getResults();

        //todo: needs code review. There must be a better way to do this
        Runnable runnable = new Runnable() {
            public void run() {
                for (final Usage r : usages) {

                    final UsageInfo2UsageAdapter usage = (UsageInfo2UsageAdapter) r;
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
                                PsiElement element = PsiUtilBase.getElementAtOffset(((UsageInfo2UsageAdapter) r).getUsageInfo().getFile(), textOffset + 1);
                                PsiElement propElement = PsiUtilBase.getElementAtOffset(((UsageInfo2UsageAdapter) r).getUsageInfo().getFile(), textOffset + 1 + "$scope".length());
                                String elementText = element.getText();
                                System.out.println(elementText + ": " + regExMatch + " - " + s);

                                result.addElement(LookupElementBuilder.create(propElement, propElement.getText()));

                                return true;
                            } catch (FindManager.MalformedReplacementStringException e1) {
                                e1.printStackTrace();
                            }

                            return false;
                        }
                    });
                }

                result.addLookupAdvertisement("Results based on all '$scope's");
                GlobalSearchScope scope = ProjectScope.getProjectScope(project);

            }
        };

        ApplicationManager.getApplication().runReadAction(runnable);

    }
}
