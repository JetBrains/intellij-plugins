package com.intellij.struts2.preview;

import com.intellij.javaee.web.DeployedFileUrlConverter;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class Struts2UrlConverter extends DeployedFileUrlConverter {

  public Collection<String> getTargetPaths(@NotNull final PsiFile sourceFile, @NotNull final WebFacet webFacet) {
    final StrutsModel combinedModel = StrutsManager.getInstance(sourceFile.getProject()).getCombinedModel(webFacet.getModule());
    @NonNls final ArrayList<String> list = new ArrayList<String>();
    if (combinedModel != null) {      
      combinedModel.processActions(new Processor<Action>() {
        public boolean process(final Action action) {
          for (final Result result : action.getResults()) {
            final PathReference pathReference = result.getValue();
            if (pathReference != null) {
              final PsiElement psiElement = pathReference.resolve();
              if (psiElement != null && psiElement.equals(sourceFile)) {
                String namespace = action.getNamespace();
                if (!namespace.equals(StrutsPackage.DEFAULT_NAMESPACE)) {
                  namespace += "/";
                }
                list.add(namespace + action.getName().getStringValue() + ".action");
              }
            }
          }
          return true;
        }
      });
    }
    return list;
  }
}
