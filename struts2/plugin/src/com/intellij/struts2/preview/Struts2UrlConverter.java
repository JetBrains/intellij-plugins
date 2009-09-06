package com.intellij.struts2.preview;

import com.intellij.javaee.web.DeployedFileUrlConverter;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class Struts2UrlConverter extends DeployedFileUrlConverter {

  public Collection<String> getTargetPaths(@NotNull final PsiFile sourceFile, @NotNull final WebFacet webFacet) {
    final StrutsModel combinedModel = StrutsManager.getInstance(sourceFile.getProject()).getCombinedModel(webFacet.getModule());
    if (combinedModel == null) {
      return Collections.emptyList();
    }

    final List<String> actionExtensions = StrutsConstantHelper.getActionExtensions(sourceFile);
    if (actionExtensions.isEmpty()) {
      return Collections.emptyList();
    }

    final String actionExtension = actionExtensions.get(0);

    @NonNls final ArrayList<String> list = new ArrayList<String>();
    combinedModel.processActions(new Processor<Action>() {
      public boolean process(final Action action) {
        for (final Result result : action.getResults()) {
          final PathReference pathReference = result.getValue();
          if (pathReference != null) {
            final PsiElement psiElement = pathReference.resolve();
            if (psiElement != null && psiElement.equals(sourceFile)) {
              String namespace = action.getNamespace();
              if (!Comparing.equal(namespace, StrutsPackage.DEFAULT_NAMESPACE)) {
                namespace += "/";
              }
              list.add(namespace + action.getName().getStringValue() + actionExtension);
            }
          }
        }
        return true;
      }
    });

    return list;
  }
}
