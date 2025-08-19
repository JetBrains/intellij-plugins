// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSUseNamespaceDirective;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ActionScriptFlexResolveUtil {

  public static Map<String, String> calculateOpenNses(PsiElement place) {
    final Ref<Map<String, String>> result = new Ref<>();
    walkOverStructure(place, psiNamedElement -> {
      if (psiNamedElement instanceof JSElement) {
        result.set(CachedValuesManager.getProjectPsiDependentCache((JSElement)psiNamedElement, ActionScriptFlexResolveUtil::doCalcOpenedNses));
      }
      return false;
    });
    return result.get() != null ? result.get(): Collections.emptyMap();
  }

  static Map<String, String> doCalcOpenedNses(JSElement context) {
    final class MyProcessor extends StructureResolveProcessor {
      Map<String, String> openedNses;

      MyProcessor() {
        super(null);
        putUserData(LOOKING_FOR_USE_NAMESPACES, true);
      }

      @Override
      public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        if (element instanceof JSPackageStatement) return false;
        if (!(element instanceof JSUseNamespaceDirective)) return true;
        if (openedNses == null) openedNses = new HashMap<>(1);
        String nsValue = ActionScriptFlexPsiImplUtil.calcNamespaceReference(element);
        if (nsValue != null) openedNses.put(nsValue, ((JSUseNamespaceDirective)element).getNamespaceToBeUsed());
        return true;
      }
    }
    MyProcessor processor = new MyProcessor();
    walkOverStructure(context, processor);
    return processor.openedNses;
  }

  public static boolean walkOverStructure(@NotNull PsiElement context, Processor<? super PsiNamedElement> processor) {
    PsiNamedElement parent = PsiTreeUtil.getNonStrictParentOfType(context, JSPackageStatement.class, JSClass.class, JSFunction.class, PsiFile.class);

    if (parent instanceof JSClass) {
      PsiElement forcedContext = JSResolveUtil.getContext(parent);

      if (forcedContext instanceof XmlBackedJSClass) {
        if (!processor.process((PsiNamedElement)forcedContext)) return false;
      }
    }

    while(parent != null) {
      if (parent instanceof JSFunctionExpression) {
        parent = PsiTreeUtil.getParentOfType(parent, JSPackageStatement.class, JSClass.class, JSFunction.class, PsiFile.class);
        continue;
      }

      if (!processor.process(parent)) return false;

      if (parent instanceof PsiFile) {
        final PsiElement data = JSResolveUtil.getContext(parent);

        if (data instanceof JSElement) {
          parent = PsiTreeUtil.getNonStrictParentOfType(data, JSPackageStatement.class, JSClass.class, JSFunction.class, PsiFile.class);
        } else {
          break;
        }
      } else {
        parent = PsiTreeUtil.getParentOfType(parent, JSPackageStatement.class, JSClass.class, JSFunction.class, PsiFile.class);
      }
    }

    return true;
  }

  public abstract static class StructureResolveProcessor extends ActionScriptResolveProcessor implements Processor<PsiNamedElement> {
    private static final ResolveProcessor.ProcessingOptions ourProcessingOptions = new JSResolveUtil.StructureProcessingOptions();

    public StructureResolveProcessor(String name) {
      super(name);
      setLocalResolve(true);
      setToProcessActionScriptImplicits(false);
      setProcessingOptions(ourProcessingOptions);
    }

    @Override
    public boolean process(PsiNamedElement psiNamedElement) {
      boolean b = true;
      if (psiNamedElement instanceof JSElement) {
        b = psiNamedElement.processDeclarations(this, ResolveState.initial(), psiNamedElement, psiNamedElement);
        PsiElement context;

        if (b && psiNamedElement instanceof JSFile && (context = psiNamedElement.getContext()) != null) {
          PsiFile containingFile = context.getContainingFile();
          if (containingFile instanceof XmlFile) b = JSResolveUtil.processAllGlobalsInXmlFile(this, (XmlFile)containingFile, context);
        }
      }
      return b;
    }
  }

}
