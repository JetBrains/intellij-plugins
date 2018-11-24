package org.angularjs.codeInsight.refs;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.include.FileIncludeManager;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Irina.Chernushina on 3/21/2016.
 */
public class AngularJSNgAppReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new AngularJSNgAppReference(((XmlAttributeValue)element))};
  }

  private static class AngularJSNgAppReference extends AngularPolyReferenceBase<XmlAttributeValue> {
    public AngularJSNgAppReference(XmlAttributeValue element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    private String getAppName() {
      return StringUtil.unquoteString(getCanonicalText());
    }

    @NotNull
    @Override
    protected ResolveResult[] resolveInner() {
      final String appName = getAppName();
      if (StringUtil.isEmptyOrSpaces(appName)) return ResolveResult.EMPTY_ARRAY;

      final CommonProcessors.CollectProcessor<JSImplicitElement> collectProcessor = new CommonProcessors.CollectProcessor<>();
      AngularIndexUtil.multiResolve(getElement().getProject(), AngularModuleIndex.KEY, appName, collectProcessor);

      Collection<JSImplicitElement> results = collectProcessor.getResults();
      if (results.size() > 1) {
        final Condition<JSImplicitElement> filter = new Condition<JSImplicitElement>() {
          private Set<VirtualFile> includedFiles;
          @Override
          public boolean value(JSImplicitElement element) {
            if (includedFiles == null) {
              final PsiFile topLevelFile =
                InjectedLanguageManager.getInstance(getElement().getProject()).getTopLevelFile(getElement().getContainingFile());
              final VirtualFile appDefinitionFile = topLevelFile.getVirtualFile();
              final VirtualFile[] includedFilesArr = FileIncludeManager.getManager(getElement().getProject()).getIncludedFiles(appDefinitionFile, true, true);
              includedFiles = new HashSet<>(Arrays.asList(includedFilesArr));
            }
            return includedFiles.contains(element.getContainingFile().getVirtualFile());
          }
        };
        results = ContainerUtil.filter(results, filter);
      }
      final List<ResolveResult> resolveResults = ContainerUtil.map(results, AngularIndexUtil.JS_IMPLICIT_TO_RESOLVE_RESULT);
      return resolveResults.toArray(ResolveResult.EMPTY_ARRAY);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
