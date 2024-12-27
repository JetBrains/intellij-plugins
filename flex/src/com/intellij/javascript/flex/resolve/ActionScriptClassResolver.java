// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.flex.JSResolveHelper;
import com.intellij.lang.javascript.index.JSIndexedRootProvider;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.indexing.AdditionalIndexedRootsScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.*;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptClassResolver extends JSClassResolver {

  private static ActionScriptClassResolver INSTANCE = null;

  private ActionScriptClassResolver() {
  }

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static ActionScriptClassResolver getInstance() {
    if (INSTANCE == null) INSTANCE = new ActionScriptClassResolver();
    return INSTANCE;
  }

  @Override
  public PsiElement findClassByQName(@NotNull String link, @NotNull PsiElement context) {
    return findClassByQNameStatic(link, context);
  }

  private @Nullable PsiElement findClassByQName(final @NotNull String link,
                                                final GlobalSearchScope searchScope,
                                                @NotNull DialectOptionHolder dialect) {
    if (searchScope instanceof JSResolveUtil.AllowFileLocalSymbols) {
      return doFindClassByQName(link, searchScope, true);
    }

    return doFindClassByQName(link, searchScope, false);
  }


  public static PsiElement findClassByQNameStatic(@NotNull String link, @NotNull PsiElement context) {
    return getInstance().findClassByQName(link, JSResolveUtil.getResolveScope(context),
                                          DialectOptionHolder.ECMA_4);
  }

  @Override
  public @Nullable PsiElement findClassByQName(@NotNull String link, @NotNull GlobalSearchScope scope) {
    return findClassByQNameStatic(link, scope);
  }

  @Override
  public @NotNull List<JSClass> findClassesByQName(@NotNull String qName, @NotNull GlobalSearchScope scope) {
    final PsiElement clazz = findClassByQName(qName, scope);
    return clazz instanceof JSClass ? Collections.singletonList((JSClass)clazz) : Collections.emptyList();
  }

  public static PsiElement findClassByQName(final @NotNull String link, final JavaScriptIndex index, final Module module) {
    GlobalSearchScope searchScope = JSInheritanceUtil.getEnforcedScope();
    if (searchScope == null) {
      searchScope =
        module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module) : GlobalSearchScope.allScope(index.getProject());
    }
    return getInstance().findClassByQName(link, searchScope, DialectOptionHolder.ECMA_4);
  }

  public static boolean isParentClass(JSClass clazz, String className) {
    return isParentClass(clazz, className, true);
  }

  public static boolean isParentClass(JSClass clazz, String className, boolean strict) {
    final PsiElement parentClass = findClassByQNameStatic(className, clazz.getResolveScope());
    if (!(parentClass instanceof JSClass)) return false;

    return JSInheritanceUtil.isParentClass(clazz, (JSClass)parentClass, strict);
  }

  @Override
  protected PsiElement doFindClassByQName(@NotNull String link, GlobalSearchScope searchScope,
                                          boolean allowFileLocalSymbols) {
    Project project = searchScope.getProject();
    boolean clazzShouldBeTakenFromOurLibrary = OBJECT_CLASS_NAME.equals(link) || "Arguments".equals(link);
    if (clazzShouldBeTakenFromOurLibrary && !(searchScope instanceof AdditionalIndexedRootsScope)) {
      // object from swf do not contain necessary members!
      searchScope = new AdditionalIndexedRootsScope(searchScope, JSIndexedRootProvider.class);
    }
    final Collection<JSQualifiedNamedElement> candidates = StubIndex.getElements(JSQualifiedElementIndex.KEY, link,
                                                                                 project, searchScope,
                                                                                 JSQualifiedNamedElement.class);
    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    JSQualifiedNamedElement resultFromSourceContent = null;
    JSQualifiedNamedElement resultFromLibraries = null;
    long resultFromLibrariesTimestamp = 0;

    for (JSQualifiedNamedElement clazz : candidates) {
      if (clazz == null || JSResolveUtil.isConstructorFunction(clazz)) continue;

      if (link.equals(clazz.getQualifiedName())) {
        PsiFile file = clazz.getContainingFile();
        if (!file.getLanguage().isKindOf(FlexSupportLoader.ECMA_SCRIPT_L4)) continue;
        VirtualFile vFile = file.getVirtualFile();
        if (clazzShouldBeTakenFromOurLibrary &&
            !JavaScriptIndex.ECMASCRIPT_JS2.equals(vFile.getName()) // object from swf do not contain necessary members!
          ) {
          continue;
        }
        if (!allowFileLocalSymbols && ActionScriptResolveUtil.isFileLocalSymbol(clazz)) {
          continue;
        }

        if (projectFileIndex.isInSourceContent(vFile)) {
          // the absolute preference is for classes from sources
          resultFromSourceContent = clazz;
          continue;
        }

        // choose the right class in the same way as compiler does: with the latest timestamp in catalog.xml file
        if (resultFromLibraries == null) {
          resultFromLibraries = clazz;
          // do not initialize resultFromLibrariesTimestamp here, it is expensive and may be not required if only 1 candidate
        }
        else if (JSCommonTypeNames.VECTOR_CLASS_NAME.equals(link)) {
          if (clazz instanceof JSClass && resultFromLibraries instanceof JSClass &&
              ((JSClass)clazz).getFunctions().length > ((JSClass)resultFromLibraries).getFunctions().length) {
            resultFromLibraries = clazz;
          }
        }
        else {
          if (resultFromLibrariesTimestamp == 0) {
            // was not initialized yet
            resultFromLibrariesTimestamp = getResolveResultTimestamp(resultFromLibraries);
          }

          final long classTimestamp = getResolveResultTimestamp(clazz);
          if (classTimestamp > resultFromLibrariesTimestamp) {
            resultFromLibraries = clazz;
            resultFromLibrariesTimestamp = classTimestamp;
          }
        }
      }
    }

    PsiElement result = resultFromSourceContent != null ? resultFromSourceContent : resultFromLibraries;
    if (result == null) {
      String className = link.substring(link.lastIndexOf('.') + 1);
      if (!className.isEmpty() && !isBuiltInClassName(className) &&
          (Character.isLetter(className.charAt(0)) || '_' == className.charAt(0))) {
        // TODO optimization, remove when packages will be properly handled
        result = findClassByQNameViaHelper(link, project, className, searchScope);
      }
    }
    return result;
  }

  private static boolean isBuiltInClassName(@NotNull String className) {
    return OBJECT_CLASS_NAME.equals(className) ||
           BOOLEAN_CLASS_NAME.equals(className) ||
           FUNCTION_CLASS_NAMES.contains(className) ||
           STRING_CLASS_NAME.equals(className);
  }

  public static PsiElement findClassByQNameStatic(final @NotNull String link, @NotNull GlobalSearchScope scope) {
    return getInstance().findClassByQName(link, scope, DialectOptionHolder.ECMA_4);
  }

  private static @Nullable PsiElement findClassByQNameViaHelper(final String link,
                                                                final Project project,
                                                                final String className,
                                                                final GlobalSearchScope scope) {
    for (JSResolveHelper helper : JSResolveHelper.EP_NAME.getExtensionList()) {
      PsiElement result = helper.findClassByQName(link, project, className, scope);
      if (result != null) return result;
    }
    return null;
  }
}

