package org.angularjs.codeInsight;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IconUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.HashSet;
import org.angularjs.AngularBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by alireza on 10/19/2016.
 */
public class AngularJSLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        if (element instanceof HtmlTag) {
            final JSImplicitElement tagDirective = DirectiveUtil.getTagDirective(((HtmlTag) element).getName(), element.getProject());
            if (tagDirective != null) {
                String [] templateUrls = getTemplateUrls(tagDirective);

                if (templateUrls.length > 0) {

                    final Project project = element.getProject();
                    Stream.of(templateUrls).forEach(templateUrl -> {
                        final String fileName = PathUtil.getFileName(templateUrl);
                        final PsiFile[] files = FilenameIndex
                                .getFilesByName(project, fileName, GlobalSearchScope.projectScope(project));

                        for (PsiFile file : files) {
                            final VirtualFile virtualFile = file.getVirtualFile();
                            if (virtualFile.getPath().endsWith(templateUrl)) {
                                String message;
                                Icon icon;

                                final ProjectFileIndex projectFileIndex = ProjectFileIndex.SERVICE.getInstance(project);
                                if(projectFileIndex.isInSource(virtualFile) &&
                                        virtualFile.getCanonicalPath().equals(projectFileIndex.getSourceRootForFile(virtualFile).getCanonicalPath() + templateUrl)){
                                    icon = AllIcons.FileTypes.Html;
                                    message = AngularBundle.message("navigation.directiveTemplate", templateUrl);
                                }
                                else{
                                    message = AngularBundle.message("navigation.directiveTemplateGuess", templateUrl);
                                    icon = IconUtil.desaturate(AllIcons.FileTypes.Html);

                                }
                                NavigationGutterIconBuilder<PsiElement> builder =  NavigationGutterIconBuilder
                                        .create(icon)
                                        .setTooltipText(message);
                                builder.setTarget(file);
                                result.add(builder.createLineMarkerInfo(element));
                            }
                        }
                    });
                    // TODO: handle template cache
                }
            }
        }
    }

    @NotNull
    private String[] getTemplateUrls(JSImplicitElement directive){
        return DirectiveUtil.getTemplateUrls(directive);
    }
    @NotNull String[] getTemplateUrls2(JSImplicitElement directive){
        Collection<String> result = new HashSet<>();
                directive.getName();
                if(directive.getParent() instanceof JSDocComment){
                    directive.getName();
                }
                else if (directive.getParent().getParent() instanceof JSArgumentList){
                    final JSExpression[] arguments = ((JSArgumentList) directive.getParent().getParent()).getArguments();
                    PsiElement directiveDefinition = arguments[arguments.length - 1];
                    directiveDefinition = resolveReference(directiveDefinition);
                    if(directiveDefinition instanceof JSFunction){
                        final JSSourceElement[] sourceElements = ((JSFunction) directiveDefinition).getBody();
                        if(sourceElements.length > 0){
                            for (PsiElement psiElement : sourceElements[0].getChildren()) {
                                if(psiElement instanceof JSReturnStatement){
                                    final PsiElement[] returnChilds = psiElement.getChildren();
                                    if(returnChilds.length > 0){
                                        final PsiElement directiveDefObject = resolveReference(returnChilds[0]);
                                        if(directiveDefObject instanceof JSObjectLiteralExpression){
                                            for (JSProperty property : ((JSObjectLiteralExpression) directiveDefObject).getProperties()) {
                                                if(StringUtil.equals(property.getName(), "templateUrl")){
                                                    if(property.getValue() instanceof JSLiteralExpression){
                                                        result.add((String) ((JSLiteralExpression) property.getValue()).getValue());
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
        return (String[]) result.toArray();
    }
    private PsiElement resolveReference(PsiElement psiElement) {
        if (psiElement instanceof JSReferenceExpression) {
            psiElement = ((JSReferenceExpression) psiElement).resolve();
        }
        return psiElement;
    }
}
