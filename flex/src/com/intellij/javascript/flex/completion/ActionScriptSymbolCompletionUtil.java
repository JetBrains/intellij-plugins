// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.javascript.flex.resolve.ActionScriptContextLevel;
import com.intellij.javascript.flex.resolve.ActionScriptTypeInfo;
import com.intellij.javascript.flex.resolve.ActionScriptVariantsProcessor;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.completion.JSCompletionUtil;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSElementType;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.JSNamespace;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSCompletionProcessor;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.LanguageStubDescriptor;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.stubs.StubTreeBuilder;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.lang.javascript.stubs.JSStubRegistryExtensionKt.shouldCreateStub;

final class ActionScriptSymbolCompletionUtil {

  /**
   * @return false if we need completely stop processing, true if we need to populate final results
   */
  public static boolean processIndexedSymbols(@NotNull PsiElement referenceElement,
                                              @NotNull ActionScriptVariantsProcessor processor,
                                              @NotNull CompletionResultSet resultSet,
                                              @Nullable List<? super Collection<LookupElement>> populatedResults) {
    boolean tooMuch = !processCompeteMatchedElements(processor);

    final Collection<LookupElement> completeMatchResults = processor.getCurrentResults();
    processor.populateCompletionList(completeMatchResults, resultSet);
    if (populatedResults != null) {
      populatedResults.add(completeMatchResults);
    }

    if (tooMuch) {
      JSCompletionUtil.handleOverflow(resultSet);
      return false;
    }

    final Project project = referenceElement.getProject();
    final PsiFile file = referenceElement.getContainingFile();
    if (!processElementsInInjectedContext(processor, file)) {
      JSCompletionUtil.handleOverflow(resultSet);
      return true;
    }

    GlobalSearchScope scope = JSResolveUtil.getResolveScope(referenceElement);
    if (!processor.addOnlyCompleteMatches() && !processAllIndexedSymbols(processor, project, scope, referenceElement)) {
      JSCompletionUtil.handleOverflow(resultSet);
      return true;
    }

    return true;
  }

  public static boolean processElementsInInjectedContext(final JSCompletionProcessor processor, PsiFile file) {
    return JSResolveUtil.tryProcessAllElementsInInjectedContext(file, base -> processor.doAdd(base));
  }

  public static boolean processCompeteMatchedElements(final @NotNull ActionScriptVariantsProcessor processor) {
    final ActionScriptTypeInfo typeInfo = processor.getTypeInfo();

    List<ActionScriptContextLevel> contextLevels = typeInfo.myContextLevels;

    GlobalSearchScope allScope = JSResolveUtil.getResolveScope(processor.getContext());
    final Set<JSNamespace> visitedNamespaces = new HashSet<>();
    for (final ActionScriptContextLevel level : contextLevels) {
      if (level.isGlobal() && !visitedNamespaces.add(level.myNamespace)) continue;
      PsiElement elementToIncludeLocalMembers = JSResolveUtil.getScopeToIncludeLocalMembers(level.myNamespace);
      PsiFile fileToIncludeLocal = elementToIncludeLocalMembers != null ? elementToIncludeLocalMembers.getContainingFile() : null;
      GlobalSearchScope scope = JSTypeUtils.isLocalOrFromSource(level.myNamespace) && fileToIncludeLocal != null
                                ? GlobalSearchScope.fileScope(fileToIncludeLocal) : allScope;
      GlobalSearchScope scopeToIncludeLocal = null;
      final Collection<JSPsiElementBase> namespaceMembers =
        JSClassResolver.getInstance().findNamespaceMembers(level.myNamespace, scope, scopeToIncludeLocal);
      for (JSPsiElementBase element : namespaceMembers) {
        if (processor.acceptsFile(element.getContainingFile())) {
          if (!processor.doAdd(element)) return false;
        }
      }
    }
    return true;
  }

  private static @Nullable Set<VirtualFile> filterIds(@NotNull Project project,
                                                      @NotNull GlobalSearchScope scope,
                                                      @NotNull PrefixMatcher matcher) {
    if (matcher.getPrefix().isEmpty()) return null;

    final Set<String> keys = new HashSet<>();

    //ActionScript class methods (e.g. Object#toString()) are stored in JSSymbolIndex2 only
    StubIndexKey<String, JSElement> indexKey = JSIndexKeys.JS_SYMBOL_INDEX_2_KEY;
    StubIndex.getInstance().processAllKeys(indexKey, s -> {
      if (matcher.prefixMatches(s)) keys.add(s);
      return true;
    }, scope);
    Set<VirtualFile> files = VfsUtilCore.createCompactVirtualFileSet();
    for (String key : keys) {
      ContainerUtil.addAll(files, StubIndex.getInstance().getContainingFiles(indexKey, key, project, scope));
    }
    return files;
  }

  public static boolean processAllFilesWithKeysMatchingPrefix(@NotNull PsiElement context,
                                                              @NotNull GlobalSearchScope scope,
                                                              @Nullable Set<VirtualFile> files,
                                                              @NotNull Processor<? super VirtualFile> processor) {
    VirtualFile originalFile = context.getContainingFile().getOriginalFile().getVirtualFile();
    if (!processor.process(originalFile)) {
      // process current file firstly
      return false;
    }

    if (files == null) {
      for (FileType fileType : JavaScriptIndex.getFileTypesToIndexJS()) {
        for (VirtualFile vFile : FileTypeIndex.getFiles(fileType, scope)) {
          ProgressManager.checkCanceled();
          if (vFile.equals(originalFile)) continue;
          if (!JavaScriptIndex.isAcceptableFile(vFile)) return true;

          if (!processor.process(vFile)) return false;
        }
      }
    }
    else {
      for (VirtualFile vFile : files) {
        ProgressManager.checkCanceled();
        if (vFile != null && !vFile.equals(originalFile) && scope.accept(vFile)) {
          if (!processor.process(vFile)) return false;
        }
      }
    }

    return true;
  }

  private static boolean processAllIndexedSymbols(@NotNull JSCompletionProcessor processor,
                                                  @NotNull Project project,
                                                  @NotNull GlobalSearchScope scope,
                                                  @NotNull PsiElement context) {
    final MyJSRecursiveWalkingElementVisitor
      visitor = new MyJSRecursiveWalkingElementVisitor(processor);
    final PsiManager psiManager = PsiManager.getInstance(project);
    @Nullable Set<VirtualFile> filterIds = filterIds(project, scope, processor.getPrefixMatcher());

    return processAllFilesWithKeysMatchingPrefix(context, scope, filterIds, vFile -> {
      if (!StubUpdatingIndex.canHaveStub(vFile)) return true;
      return processAllInVirtualFile(processor, psiManager, visitor, vFile);
    });
  }

  private static boolean processAllInVirtualFile(@NotNull JSCompletionProcessor processor,
                                                 @NotNull PsiManager psiManager,
                                                 @NotNull MyJSRecursiveWalkingElementVisitor visitor,
                                                 @NotNull VirtualFile vFile) {
    final PsiFile psi = psiManager.findFile(vFile);
    if (psi == null) return true;
    if (!processor.acceptsFile(psi)) return true;

    return processSymbolsInPsiFile(processor, visitor, psi);
  }

  private static boolean processSymbolsInPsiFile(@NotNull JSCompletionProcessor processor,
                                                 MyJSRecursiveWalkingElementVisitor visitor,
                                                 PsiFile psi) {
    @Unmodifiable @NotNull List<Pair<LanguageStubDescriptor, PsiFile>> stubbedRoots = StubTreeBuilder.getStubbedRootDescriptors(psi.getViewProvider());
    for (Pair<LanguageStubDescriptor, PsiFile> stubbedRoot : stubbedRoots) {
      PsiFile root = stubbedRoot.second;
      ((PsiFileImpl)root).withGreenStubTreeOrAst(
        tree -> {
          //System.err.println("stubbed file access: " + psi.getName());
          for (StubElement<?> stubElement : tree.getPlainList()) {
            ProgressManager.checkCanceled();
            final IElementType type = stubElement.getElementType();
            if (type instanceof JSElementType<?>) {
              //noinspection unchecked
              JSElementType<? super JSElement> jsStubType = (JSElementType<? super JSElement>)type;
              final PsiElement psiElement = stubElement.getPsi();
              if (!processElement(processor, (JSElement)psiElement, jsStubType)) return false;
            }
          }
          return null;
        },
        ast -> {
          //System.err.println("psi file access: " + psi.getName());
          root.accept(visitor);
          return null;
        }
      );
    }
    return true;
  }

  private static <PsiT extends JSElement> boolean processElement(JSCompletionProcessor processor,
                                                                 PsiT element,
                                                                 JSElementType<PsiT> type) {
    if (element instanceof JSFunctionExpression) return true;

    if (element instanceof JSPsiElementBase && !StringUtil.isEmpty(element.getName()) && type.shouldIndexSymbol(element)) {
      if (!processor.doAdd((JSPsiElementBase)element)) return false;
    }

    return true;
  }

  private static class MyJSRecursiveWalkingElementVisitor extends JSRecursiveWalkingElementVisitor {

    private final JSCompletionProcessor myProcessor;

    MyJSRecursiveWalkingElementVisitor(JSCompletionProcessor processor) {
      myProcessor = processor;
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
      final ASTNode node = element.getNode();
      final IElementType type = node != null ? node.getElementType() : null;
      if (type instanceof JSElementType<?>) {
        if (shouldCreateStub(type, node)) {
          //noinspection unchecked
          if (!processElement(myProcessor, (JSElement)element, (JSElementType<JSElement>)type)) stopWalking();
        }
      }
      super.visitElement(element);
    }
  }
}
