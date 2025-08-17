// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.validation;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.flex.PredefinedImportSet;
import com.intellij.lang.javascript.flex.ScopedImportSet;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSImportedElementResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// TODO [ksafonov] think about working on a single Result instance instead of merging Result-s

public final class ActionScriptUnusedImportsHelper {

  public static final class Results {
    public final Collection<JSImportStatement> unusedImports;
    public final MultiMap<Computable<JSElement>, String> importsByHolder;
    public final Collection<JSReferenceExpression> fqnsToReplaceWithShortName;
    public final MultiMap<JSImportStatement, JSReferenceExpression> usedImports;


    private Results(Collection<JSReferenceExpression> fqnsToReplaceWithShortName,
                    Collection<JSImportStatement> unusedImports,
                    MultiMap<Computable<JSElement>, String> importsByHolder,
                    MultiMap<JSImportStatement, JSReferenceExpression> usedImports) {
      this.fqnsToReplaceWithShortName = fqnsToReplaceWithShortName;
      this.unusedImports = unusedImports;
      this.importsByHolder = importsByHolder;
      this.usedImports = usedImports;
    }

    private Results() {
      this(new ArrayList<>(), new HashSet<>(),
           MultiMap.createSet(),
           MultiMap.createSet());
    }

    private void merge(Results results) {
      fqnsToReplaceWithShortName.addAll(results.fqnsToReplaceWithShortName);
      results.unusedImports.removeAll(usedImports.keySet());
      unusedImports.addAll(results.unusedImports);
      for (Computable<JSElement> holder : results.importsByHolder.keySet()) {
        for (String s : results.importsByHolder.get(holder)) {
          importsByHolder.putValue(holder, s);
        }
      }
      for (JSImportStatement anImport : results.usedImports.keySet()) {
        usedImports.put(anImport, results.usedImports.get(anImport));
        unusedImports.remove(anImport);
      }
    }

    public Collection<JSImportStatement> getAllImports() {
      List<JSImportStatement> result = new ArrayList<>(unusedImports);
      result.addAll(usedImports.keySet());
      return result;
    }
  }

  private static final Key<CachedValue<Results>> ourUnusedImportsKey = Key.create("js.unused.imports");

  private final Set<JSImportStatement> myUnused = new HashSet<>();
  private final Collection<JSReferenceExpression> fqnsToReplaceWithImport = new ArrayList<>();
  private final PsiFile myContainingFile;
  private final Collection<PsiElement> myElements;
  private final MultiMap<JSImportStatement, JSReferenceExpression> myUsed = MultiMap.createSet();

  private ActionScriptUnusedImportsHelper(PsiFile containingFile, Collection<PsiElement> elements) {
    myContainingFile = containingFile;
    myElements = elements;
  }

  private void registerUnused(final JSImportStatement importStatement) {
    if (!myUsed.containsKey(importStatement) && importStatement.getImportText() != null) {
      myUnused.add(importStatement);
    }
  }

  private void process(JSReferenceExpression node) {
    if (node.getQualifier() == null) {
      String thisPackage = JSResolveUtil.findPackageStatementQualifier(node);
      registerUsedImportsFromResolveResults(node, thisPackage);
    }
    else {
      if (PsiTreeUtil.getParentOfType(node, JSImportStatement.class) != null) {
        return;
      }
      if (node.getParent() instanceof JSClass &&
          node.getPrevSibling() instanceof PsiWhiteSpace &&
          node.getPrevSibling().getPrevSibling() != null &&
          node.getPrevSibling().getPrevSibling().getNode().getElementType() == JSTokenTypes.CLASS_KEYWORD) {
        return;
      }

      JSReferenceExpression topReference = JSResolveUtil.getTopReferenceExpression(node);
      if (topReference.getParent() instanceof JSPackageStatement &&
          topReference.getPrevSibling() instanceof PsiWhiteSpace &&
          topReference.getPrevSibling().getPrevSibling() != null &&
          topReference.getPrevSibling().getPrevSibling().getNode().getElementType() == JSTokenTypes.PACKAGE_KEYWORD) {
        return;
      }
      registerUsedImportsFromResolveResults(node, null);

      Couple<Boolean> replaceStatus = UnusedImportsUtil.getReplaceStatus(node);

      if (replaceStatus.second) {
        if (sameContainingFile(node.getContainingFile(), myContainingFile)) {
          fqnsToReplaceWithImport.add(node);
        }
      }
    }
  }

  private static @Nullable JSImportStatement getUsedImportStatement(JSReferenceExpression node, String thisPackage) {
    for (ResolveResult r : node.multiResolve(false)) {
      // TODO can we get different import statements here?
      if (r instanceof JSResolveResult) {
        JSImportStatement importStatement = ((JSResolveResult)r).getActionScriptImport();

        if (importStatement != null && UnusedImportsUtil.isInstance(r.getElement(), UnusedImportsUtil.REFERENCED_ELEMENTS_CLASSES)) {
          String importString = importStatement.getImportText();
          String importedPackage = StringUtil.getPackageName(importString);
          if (thisPackage == null || !thisPackage.equals(importedPackage)) {
            return importStatement;
          }
        }
      }
    }
    return null;
  }

  private void registerUsedImportsFromResolveResults(JSReferenceExpression node, String thisPackage) {
    final JSImportStatement s = getUsedImportStatement(node, thisPackage);
    if (s != null) registerUsed(s, node);
  }

  private static boolean sameContainingFile(PsiFile file1, PsiFile file2) {
    PsiFile containing1 = getContainingFile(file1);
    PsiFile containing2 = getContainingFile(file2);
    return containing1 == containing2;
  }

  private void registerUsed(JSImportStatement importStatement, JSReferenceExpression node) {
    assert importStatement.getImportText() != null;

    myUnused.remove(importStatement);
    myUsed.putValue(importStatement, node);
  }

  private Collection<JSImportStatement> filter(Collection<JSImportStatement> original) {
    Collection<JSImportStatement> result = new ArrayList<>();
    for (JSImportStatement importStatement : original) {
      if (isAcceptable(importStatement)) {
        result.add(importStatement);
      }
    }
    return result;
  }

  private MultiMap<JSImportStatement, JSReferenceExpression> filter(MultiMap<JSImportStatement, JSReferenceExpression> original) {
    MultiMap<JSImportStatement, JSReferenceExpression> result = MultiMap.createSet();
    for (JSImportStatement importStatement : original.keySet()) {
      if (isAcceptable(importStatement)) {
        result.put(importStatement, original.get(importStatement));
      }
    }
    return result;
  }

  private boolean isAcceptable(JSImportStatement importStatement) {
    return importStatement.isValid() && sameContainingFile(importStatement.getContainingFile(), myContainingFile);
  }


  public static Results getUnusedImports(PsiFile file) {
    final PsiFile containingFile = getContainingFile(file);

    CachedValue<Results> data = containingFile.getUserData(ourUnusedImportsKey);
    if (data == null) {
      data = CachedValuesManager.getManager(file.getProject()).createCachedValue(() -> {
        final Map<XmlTag, Collection<PsiElement>> allElements = new HashMap<>();
        Collection<JSFile> processedFiles = new HashSet<>();
        collectElements(containingFile, allElements, processedFiles, null);

        Results allResults = new Results();
        for (Collection<PsiElement> elements : allElements.values()) {
          allResults.merge(new ActionScriptUnusedImportsHelper(containingFile, elements).getUnusedImports());
        }

        // TODO explicit depencencies
        return new CachedValueProvider.Result<>(allResults, PsiModificationTracker.MODIFICATION_COUNT);
      }, false);
      containingFile.putUserData(ourUnusedImportsKey, data);
    }
    return data.getValue();
  }

  private static PsiFile getContainingFile(PsiFile file) {
    return file.getContext() != null ? file.getContext().getContainingFile() : file;
  }

  private Results getUnusedImports() {
    for (PsiElement e : myElements) {
      if (e instanceof JSImportStatement importStatement) {
        registerUnused(importStatement);
      }
      else if (e instanceof JSReferenceExpression) {
        process((JSReferenceExpression)e);
      }
    }

    MultiMap<Computable<JSElement>, String> importsByHolder = MultiMap.createSet();
    for (JSImportStatement anImport : myUsed.keySet()) {
      if (!isAcceptable(anImport)) continue;
      Computable<JSElement> importHolder =
        ImportUtils.getImportHolder(anImport, true, JSFunction.class, JSPackageStatement.class, JSFile.class);
      assert importHolder != null : "Import holder not found for " + anImport.getText();
      addImport(importsByHolder, importHolder, anImport.getImportText());
    }

    final List<JSReferenceExpression> replaceWithShortName = new ArrayList<>();
    for (JSReferenceExpression qualifiedReference : fqnsToReplaceWithImport) {
      final Collection<String> imports;
      final Computable<JSElement> importHolder;

      Computable<JSElement> enclosingFunction = ImportUtils.getImportHolder(qualifiedReference, false, JSFunction.class);
      Computable<JSElement> enclosingPackage = ImportUtils.getImportHolder(qualifiedReference, false, JSPackageStatement.class);

      if (enclosingFunction != null && !importsByHolder.get(enclosingFunction).isEmpty()) {
        importHolder = enclosingFunction;
        imports = new HashSet<>(importsByHolder.get(enclosingFunction));
        imports.addAll(importsByHolder.get(enclosingPackage));
      }
      else if (enclosingPackage != null) {
        importHolder = enclosingPackage;
        imports = importsByHolder.get(enclosingPackage);
      }
      else {
        importHolder = ImportUtils.getImportHolder(qualifiedReference, false, JSFile.class);
        imports = importsByHolder.get(importHolder);
      }

      // first, get all results considering current import statements, that are used
      final Collection<String> qnames = new HashSet<>();
      String referencedName = qualifiedReference.getReferencedName();
      if (referencedName != null) {
        for (ResolveResult result : JSReferenceExpressionImpl.resolveUnqualified(referencedName, qualifiedReference, null)) {
          if (result instanceof JSResolveResult) {
            if (!myUnused.contains(((JSResolveResult)result).getActionScriptImport())) {
              PsiElement element = (result).getElement();
              if (qualifiedReference.getParent() instanceof JSNewExpression &&
                  element instanceof JSFunction &&
                  ((JSFunction)element).isConstructor()) {
                PsiElement parent = element.getParent();
                if (parent instanceof JSQualifiedNamedElement) {
                  element = parent;
                }
              }
              qnames.add(((JSQualifiedNamedElement)element).getQualifiedName());
            }
          }
        }
      }
      // then all those that result from the import statements we will insert
      PredefinedImportSet predefinedImportSet = new PredefinedImportSet(imports);
      predefinedImportSet.process(qualifiedReference.getReferencedName(), null, myContainingFile, new ScopedImportSet.ImportProcessor<>() {
        @Override
        public Object process(@NotNull String referenceName, @NotNull ImportInfo info, @NotNull PsiNamedElement scope) {
          final JSImportedElementResolveResult elementResolveResult = ScopedImportSet.resolveImportedClass(referenceName, scope, info);
          if (elementResolveResult != null) qnames.add(elementResolveResult.qualifiedName);
          return null;
        }
      });

      final String fqn = qualifiedReference.getText();

      if (qnames.isEmpty()) {
        replaceWithShortName.add(qualifiedReference);
        importsByHolder.putValue(importHolder, fqn);
      }
      else if (qnames.size() == 1 && fqn.equals(ContainerUtil.getFirstItem(qnames, null))) {
        replaceWithShortName.add(qualifiedReference);
      }
    }
    return new Results(replaceWithShortName, filter(myUnused), importsByHolder, filter(myUsed));
  }

  private static void addImport(MultiMap<Computable<JSElement>, String> importsByHolder,
                                Computable<JSElement> importHolder,
                                String newImport) {
    String newPackageName = StringUtil.getPackageName(newImport);
    String newShortName = StringUtil.getShortName(newImport);

    for (Iterator<String> i = importsByHolder.get(importHolder).iterator(); i.hasNext();) {
      String existing = i.next();
      String existingPackageName = StringUtil.getPackageName(existing);
      String existingShortName = StringUtil.getShortName(existing);
      if (existingPackageName.equals(newPackageName)) {
        if ("*".equals(existingShortName) && !"*".equals(newShortName)) {
          return;
        }
        else if ("*".equals(newShortName)) {
          i.remove();
        }
      }
    }
    importsByHolder.putValue(importHolder, newImport);
  }

  private static void collectElements(final PsiFile file,
                                      final Map<XmlTag, Collection<PsiElement>> result,
                                      final Collection<JSFile> processedFiles,
                                      final @Nullable XmlTag rootTag) {
    if (processedFiles.contains(file)) {
      return;
    }
    if (file instanceof JSFile) {
      processedFiles.add((JSFile)file);

      PsiTreeUtil.processElements(file, new PsiElementProcessor<>() {
        @Override
        public boolean execute(@NotNull PsiElement element) {
          if (element instanceof JSIncludeDirective) {
            PsiFile includedFile = ((JSIncludeDirective)element).resolveFile();
            // we check processed files before since we may include this file to self and setting context will make cycle
            if (includedFile instanceof JSFile && !processedFiles.contains((JSFile)includedFile)) {
              includedFile.putUserData(JSResolveUtil.contextKey, element);
              collectElements(includedFile, result, processedFiles, rootTag);
            }
          }
          else if (element instanceof JSElement && !(element instanceof JSFile)) {
            Collection<PsiElement> elements = result.get(rootTag);
            if (elements == null) {
              elements = new ArrayList<>();
              result.put(rootTag, elements);
            }
            elements.add(element);
          }
          return true;
        }
      });
    }
    else {
      XmlBackedJSClass jsClass = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)file);
      if (jsClass != null) {
        jsClass.visitInjectedFiles(new XmlBackedJSClass.InjectedFileVisitor() {
          @Override
          public void visit(XmlTag rootTag, JSFile jsFile) {
            collectElements(jsFile, result, processedFiles, rootTag);
          }
        });
      }
    }
  }
}
