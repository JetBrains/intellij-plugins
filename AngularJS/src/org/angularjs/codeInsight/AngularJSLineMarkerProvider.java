package org.angularjs.codeInsight;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ConstantFunction;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.HashSet;
import org.angularjs.AngularBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by alireza on 10/19/2016.
 */
public class AngularJSLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
        for (PsiElement psiElement : list) {
            collectSlowLineMarkers(psiElement, collection);
        }
    }

    private void collectSlowLineMarkers(@NotNull PsiElement element, @NotNull Collection<LineMarkerInfo> result){
        if (element instanceof HtmlTag) {
            final JSImplicitElement directive = DirectiveUtil.getTagDirective(((HtmlTag) element).getName(), element.getProject());
            if (directive != null) {
                String [] templateUrls = getTemplateUrls(directive);

                if(templateUrls.length > 0){
                    final String tooltip = AngularBundle.message("navigation.directiveTemplates") + " (" +
                            ((HtmlTag) element).getName() + ")";

                    result.add(new LineMarkerInfo<PsiElement>(
                            element,element.getTextRange(),
                            AllIcons.FileTypes.Html,
                            Pass.UPDATE_FOLDING,
                            new ConstantFunction<>(tooltip),
                            new MyNavigationHandler(directive),
                            GutterIconRenderer.Alignment.RIGHT)
                    );
                }

            }
        }
    }

    @NotNull
    private String[] getTemplateUrls(JSImplicitElement directive){
        return DirectiveUtil.getTemplateUrls(directive);
    }

    private class MyNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
        private JSImplicitElement directive;

        public MyNavigationHandler(JSImplicitElement directive) {
            this.directive = directive;
        }

        @Override
        public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
            final List<GotoRelatedItem> items = getItems();
            if(items.size() == 1){
                items.get(0).navigate();
            }
            else{
                final String name = AngularBundle.message("navigation.directiveTemplates");
                NavigationUtil.getRelatedItemsPopup(items, name).show(new RelativePoint(mouseEvent));
            }
        }

        private List<GotoRelatedItem> getItems(){
            List<GotoRelatedItem> items = new ArrayList<>();
            final Project project = directive.getProject();
            String [] templateUrls = getTemplateUrls(directive);
            Arrays.stream(templateUrls).forEach(templateUrl -> {
                final String fileName = PathUtil.getFileName(templateUrl);
                final PsiFile[] files = FilenameIndex
                        .getFilesByName(project, fileName, GlobalSearchScope.projectScope(project));

                for (PsiFile file : files) {
                    // TODO: is there a more optimized way?
                    // NOTE: Usually templateUrl fields are relative to a source folder inside directory hierarchy of
                    // the project and possibly this folder is not marked as a source or resource **root**.
                    final VirtualFile virtualFile = file.getVirtualFile();
                    if (virtualFile.getPath().endsWith(templateUrl)) {
                        items.add(new GotoRelatedItem(file, directive.getName()));
                    }
                }
            });
            // TODO: handle template cache
            return items;
        }
    }
}
