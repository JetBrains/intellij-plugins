// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript;

import com.intellij.codeInsight.completion.CompletionUtilCoreImpl;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSBlockStatement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptDocReferenceSet;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptReferenceSet;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.impl.JSReferenceSet;
import com.intellij.lang.javascript.psi.impl.JSTextReference;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagNamepath;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagType;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSTagContextBuilder;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.lang.javascript.psi.resolve.ResultSink;
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlToken;
import org.jetbrains.annotations.NotNull;

public final class ActionScriptTextReferenceResolver {

  public static ResolveResult @NotNull [] resolve(@NotNull JSTextReference reference) {
    PsiFile psiFile = reference.getSet().getElement().getContainingFile();
    String text = reference.getCanonicalText();
    MyResolveProcessor processor = new MyResolveProcessor(text, psiFile, new ResolveResultSink(psiFile, text), reference);
    doProcess(reference, psiFile, processor);
    return processor.getResultsAsResolveResults();
  }

  public static void processToSink(@NotNull JSTextReference reference, @NotNull PsiFile containingFile, @NotNull ResultSink resultSink) {
    SinkResolveProcessor<ResultSink> processor = new MyResolveProcessor(resultSink.getName(), resultSink.place, resultSink, reference);
    doProcess(reference, containingFile, processor);
  }

  private static void doProcess(JSTextReference reference, PsiFile psiFile, SinkResolveProcessor<ResultSink> processor) {
    PsiElement nearestClass = findNearestClass(reference);

    int index = calcMyIndex(reference);
    JSReferenceSet referenceSet = reference.getSet();
    if (index == 0) {
      if ("this".equals(reference.getCanonicalText()) && nearestClass instanceof JSClass) {
        processor.addResult(nearestClass);
        return;
      }
      if (reference.getRangeInElement().getStartOffset() > 0) {
        if (nearestClass instanceof JSClass && !(referenceSet.getElement() instanceof JSLiteralExpression)) {
          processor.setToProcessHierarchy(true);
          processor.setTypeContext(true);
          processor.configureClassScope((JSClass)nearestClass);
          if(!nearestClass.processDeclarations(processor, ResolveState.initial(), nearestClass, nearestClass)) return;
        }
      }
      else if (nearestClass instanceof JSClass) {
        processor.setTypeContext(true);
        processor.setToProcessMembers(false);
        if(!nearestClass.processDeclarations(processor, ResolveState.initial(), nearestClass, nearestClass)) return;

        boolean isOnlyFqns = isOnlyFqns(referenceSet);

        if (!isOnlyFqns && (referenceSet.getElement() instanceof JSDocTagType || referenceSet.getElement() instanceof JSDocTagNamepath)) {
          String packageName = JSResolveUtil.getPackageName(nearestClass);
          processor.setForcedPackageName(packageName);
          String classQName = !packageName.isEmpty() ? packageName + "." + reference.getCanonicalText() : reference.getCanonicalText();
          PsiElement clazz =
            JSDialectSpecificHandlersFactory.forElement(nearestClass).getClassResolver().findClassByQName(classQName, nearestClass);
          if(clazz != null && !clazz.processDeclarations(processor, ResolveState.initial(), clazz, clazz)) return;
        }
      }

      PsiElement startFrom = referenceSet.getElement();
      PsiElement lastParent = startFrom.getParent();
      if (startFrom instanceof JSLiteralExpression) {
        JSFunction fun = PsiTreeUtil.getParentOfType(startFrom, JSFunction.class);
        while(fun != null) {
          startFrom = fun;
          lastParent = fun.getParent();
          fun = PsiTreeUtil.getParentOfType(fun, JSFunction.class);
        }
      }
      JSResolveUtil.treeWalkUp(processor, startFrom, lastParent, referenceSet.getElement());
      if (!(psiFile instanceof JSFile) && !(psiFile instanceof XmlFile) && psiFile.getContext() == null) {
        ActionScriptResolveUtil.processGlobalThings(processor, ResolveState.initial(), psiFile, psiFile);
      }
    }
    else {
      PsiElement psiElement = referenceSet.getReferences()[index - 1].resolve();

      if (psiElement instanceof JSOffsetBasedImplicitElement &&
          ((JSOffsetBasedImplicitElement)psiElement).getType() == JSImplicitElement.Type.Tag) {
        psiElement = ((JSOffsetBasedImplicitElement)psiElement).getElementAtOffset();
      }
      if (psiElement instanceof XmlToken) {
        final JSTagContextBuilder
          builder = new JSTagContextBuilder(psiElement, BaseJSSymbolProcessor.HTML_ELEMENT_TYPE_NAME);
        psiElement = builder.element;
      }
      if (psiElement != null) {
        JSType type = null;

        if (psiElement instanceof JSVariable) {
          type = ((JSVariable)psiElement).getJSType();
        }
        else if (psiElement instanceof JSFunction) {
          if (((JSFunction)psiElement).isGetProperty()) {
            type = ((JSFunction)psiElement).getReturnType();
          }
        }

        if (type != null) {
          String typeText = type.getTypeText();
          typeText = JSTypeUtils.getTypeMatchingNamespace(psiElement.getProject(), typeText);
          String qname = JSImportHandlingUtil.resolveTypeName(typeText, psiElement);
          assert qname != null;
          PsiElement typeClass = JSClassResolver.findClassFromNamespace(qname, psiElement);
          if (typeClass instanceof JSClass) {
            psiElement = typeClass;
          }
        }
        if (psiElement instanceof JSClass) processor.setToProcessHierarchy(true);
        String packageName = null;
        if (psiElement instanceof JSPackage) {
          packageName = ((JSPackage)psiElement).getQualifiedName();
        }
        else if (psiElement instanceof JSClass) {
          packageName = JSResolveUtil.getPackageName(psiElement);
        }
        if (packageName != null) processor.setForcedPackageName(packageName);
        if (psiElement instanceof JSClass) {
          processor.configureClassScope((JSClass)psiElement);
          processor.setAllowUnqualifiedStaticsFromInstance(true);
        }
        psiElement.processDeclarations(processor, ResolveState.initial(), psiElement, psiElement);
      }
    }

    if (psiFile instanceof XmlFile && !FlexSupportLoader.isMxmlOrFxgFile(psiFile)) {
      // TODO: short names during completion should be
      ActionScriptResolveUtil.processTopLevelClasses(
        processor,
        ResolveState.initial(),
        psiFile.getProject(),
        JSResolveUtil.getResolveScope(psiFile),
        isOnlyFqns(referenceSet) ? ActionScriptResolveUtil.GlobalSymbolsAcceptanceState.ACCEPT_ONLY_CLASSES : ActionScriptResolveUtil.GlobalSymbolsAcceptanceState.WHATEVER,
        false
      );
    }
  }

  private static boolean isOnlyFqns(@NotNull JSReferenceSet referenceSet) {
    return referenceSet instanceof ActionScriptReferenceSet && ((ActionScriptReferenceSet)referenceSet).isOnlyFqns();
  }

  private static int calcMyIndex(JSTextReference reference) {
    int i = 0;
    while (i < reference.getSet().getReferences().length && reference.getSet().getReferences()[i] != reference) {
      ++i;
    }
    return i;
  }

  private static PsiElement findNearestClass(JSTextReference reference) {
    PsiElement elt = reference.getSet().getElement();
    PsiElement candidateBlock = null;
    PsiElement parent;
    while (!((parent = elt.getParent()) instanceof JSFile) && !(parent instanceof JSPackageStatement)) {
      if (parent instanceof XmlTagChild) break;
      if (parent instanceof JSBlockStatement) {
        candidateBlock = elt;
      }
      elt = parent;
      if (elt == null || elt instanceof JSClass) break;
    }

    if (parent instanceof XmlTag) {
      if (XmlBackedJSClassImpl.isComponentTag((XmlTag)parent)) {
        final XmlTag[] subtags = ((XmlTag)parent).getSubTags();
        // TODO check: if element is under tag, that tag is the first subtag?
        if (subtags.length > 0) {
          elt = XmlBackedJSClassFactory.getInstance().getXmlBackedClass(subtags[0]);
        }
      } else {
        XmlFile xmlFile = (XmlFile)parent.getContainingFile();
        if (FlexSupportLoader.isMxmlOrFxgFile(xmlFile))
          elt = XmlBackedJSClassFactory.getXmlBackedClass(xmlFile);
      }
    }

    if (elt != null && !(elt instanceof JSClass)) {
      if (candidateBlock != null) {
        elt = candidateBlock;
      }
      //elt = elt.getNextSibling();
      //if (elt instanceof PsiWhiteSpace) elt = elt.getNextSibling();
    }

    if (elt != null) {
      PsiElement originalElement = CompletionUtilCoreImpl.getOriginalElement(elt);
      if (originalElement != null) elt = originalElement;
    }
    return elt;
  }

  private static final class MyResolveProcessor extends SinkResolveProcessor<ResultSink> {
    private String normalizedQN;
    private final JSTextReference myReference;

    MyResolveProcessor(String name,
                              PsiElement _place,
                              @NotNull ResultSink sink,
                              JSTextReference reference) {
      super(name, _place, sink);
      myReference = reference;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
      JSReferenceSet referenceSet = myReference.getSet();
      if (referenceSet instanceof ActionScriptReferenceSet asReferenceSet && asReferenceSet.isOnlyFqns()) {
        if(!(element instanceof JSPackage) && !(element instanceof JSClass)) return true;
        if (myName != null &&
            element instanceof JSClass &&
            myName.equals(((JSClass)element).getName())) {
          String referenceText = asReferenceSet.getReferenceText();
          if (normalizedQN == null && referenceText != null) {
            normalizedQN = StringUtil.unquoteString(referenceText).replace(':', '.');
          }
          if (normalizedQN != null && !normalizedQN.equals(((JSClass)element).getQualifiedName())) return true;
        }
      }
      if (referenceSet instanceof ActionScriptDocReferenceSet docReferenceSet &&
          docReferenceSet.isOnlyDefaultPackage() &&
          element instanceof JSQualifiedNamedElement) {
        String qName = ((JSQualifiedNamedElement)element).getQualifiedName();
        if (qName != null && !StringUtil.isEmpty(StringUtil.getPackageName(qName))) return true;
      }
      return super.execute(element, state);
    }
  }

}
