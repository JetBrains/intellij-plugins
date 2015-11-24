package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationRegion;
import com.jetbrains.lang.dart.analyzer.DartServerData.DartNavigationTarget;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartResolver implements ResolveCache.AbstractResolver<DartReference, List<? extends PsiElement>> {
  public static final DartResolver INSTANCE = new DartResolver();

  public static boolean isServerDrivenResolution() {
    return Registry.is("dart.server.driven.resolution", false);
  }

  @Nullable
  @Override
  public List<? extends PsiElement> resolve(@NotNull DartReference reference, boolean incompleteCode) {
    if (isServerDrivenResolution()) {
      reference = replaceQualifiedReferenceWithLast(reference);
      final PsiFile refPsiFile = reference.getContainingFile();
      int refOffset = reference.getTextRange().getStartOffset();
      int refLength = reference.getTextRange().getLength();
      DartServerData.DartNavigationRegion region = findRegion(refPsiFile, refOffset, refLength);

      if (region == null && reference instanceof DartLibraryId) {
        // DAS returns the whole "part of foo" as a region, but we have only "foo" as a reference
        final PsiElement parent = reference.getParent();
        if (parent instanceof DartPartOfStatement) {
          refOffset = parent.getTextRange().getStartOffset();
          refLength = reference.getTextRange().getEndOffset() - refOffset;
          region = findRegion(refPsiFile, refOffset, refLength);
        }
      }

      if (region == null && (reference instanceof DartSuperExpression || reference instanceof DartReferenceExpression)) {
        // DAS from SDK 1.13- returns 'super.foo' as a single range; SDK 1.14 returns 'super' and 'foo' separately.
        final PsiElement parent = reference.getParent();
        if (parent instanceof DartSuperCallOrFieldInitializer) {
          final List<DartExpression> expressions = ((DartSuperCallOrFieldInitializer)parent).getExpressionList();
          if (expressions.size() == 2 &&
              expressions.get(0) instanceof DartSuperExpression &&
              expressions.get(1) instanceof DartReferenceExpression) {
            refOffset = expressions.get(0).getTextRange().getStartOffset();
            refLength = expressions.get(1).getTextRange().getEndOffset() - refOffset;
            region = findRegion(refPsiFile, refOffset, refLength);
          }
        }
      }

      if (region != null) {
        final Project project = reference.getProject();
        final List<PsiElement> result = new SmartList<PsiElement>();
        for (DartNavigationTarget target : region.getTargets()) {
          final PsiElement targetElement = getElementForNavigationTarget(project, target);
          if (targetElement != null) {
            result.add(targetElement);
          }
        }
        return result;
      }
      return null;
    }

    if (reference instanceof DartThisExpression) {
      return toResult(PsiTreeUtil.getParentOfType(reference, DartClass.class));
    }
    if (reference instanceof DartParameterNameReferenceExpression) {
      PsiElement target;
      {
        final DartCallExpression callExpression = PsiTreeUtil.getParentOfType(reference, DartCallExpression.class);
        final DartExpression expression = callExpression != null ? callExpression.getExpression() : null;
        target = expression instanceof DartReference ? ((DartReference)expression).resolve() : null;
        target = target != null ? target.getParent() : null;
      }
      if (target == null) {
        final DartNewExpression newExpression = PsiTreeUtil.getParentOfType(reference, DartNewExpression.class);
        if (newExpression != null) {
          target = DartResolveUtil.findConstructorDeclaration(newExpression);
        }
      }
      final DartFormalParameterList parameters = PsiTreeUtil.getChildOfType(target, DartFormalParameterList.class);
      return toResult(DartResolveUtil.findParameterByName(parameters, reference.getText()));
    }
    if (DartResolveUtil.aloneOrFirstInChain(reference)) {
      return resolveSimpleReference(reference);
    }
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);
    // reference [node, node]
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(reference, DartReference.class);
    if (references != null && references.length == 2) {
      // import prefix
      final List<DartComponentName> result = new SmartList<DartComponentName>();
      final String importPrefix = references[0].getCanonicalText();
      final String componentName = references[1].getCanonicalText();
      DartResolveUtil
        .processDeclarationsInImportedFileByImportPrefix(reference, importPrefix, new DartResolveProcessor(result, componentName),
                                                         componentName);
      if (!result.isEmpty()) {
        return result;
      }

      return toResult(references[1].resolve());
    }
    else if (leftReference != null) {
      final DartClassResolveResult classResolveResult = leftReference.resolveDartClass();
      final DartClass dartClass = classResolveResult.getDartClass();
      if (dartClass != null) {
        final String name = reference.getCanonicalText();
        final DartComponent subComponent = leftReference instanceof DartType
                                           ? dartClass.findNamedConstructor(name)
                                           : filterAccess(reference, dartClass.findMembersByName(name));
        return toResult(subComponent == null ? null : subComponent.getComponentName());
      }

      // import prefix
      final List<DartComponentName> result = new SmartList<DartComponentName>();
      final String importPrefix = leftReference.getCanonicalText();
      final String componentName = reference.getCanonicalText();
      DartResolveUtil
        .processDeclarationsInImportedFileByImportPrefix(reference, importPrefix, new DartResolveProcessor(result, componentName),
                                                         componentName);
      if (!result.isEmpty()) {
        return result;
      }
    }

    return null;
  }

  /**
   * When parameter information is requested for <code>items.insert(^)</code>,
   * we are given <code>items.insert</code>, but we cannot resolve it, we need just <code>insert</code>.
   */
  @NotNull
  private static DartReference replaceQualifiedReferenceWithLast(@NotNull DartReference reference) {
    final PsiElement lastChild = reference.getLastChild();
    if (lastChild instanceof DartReference) {
      reference = (DartReference)lastChild;
    }
    return reference;
  }

  @Nullable
  public static DartNavigationRegion findRegion(final PsiFile refPsiFile, final int refOffset, final int refLength) {
    final VirtualFile refVirtualFile = DartResolveUtil.getRealVirtualFile(refPsiFile);
    if (refVirtualFile != null) {
      final List<DartServerData.DartNavigationRegion> regions = DartAnalysisServerService.getInstance().getNavigation(refVirtualFile);
      return findRegion(regions, refOffset, refLength);
    }
    return null;
  }

  @Nullable
  private static PsiElement getElementForNavigationTarget(Project project, DartNavigationTarget target) {
    String targetPath = target.getFile();
    PsiFile file = findPsiFile(project, targetPath);
    if (file != null) {
      int targetOffset = target.getOffset();

      PsiElement elementAt = PsiTreeUtil.findElementOfClassAtOffset(file, targetOffset, DartComponentName.class, false);
      if (elementAt == null) elementAt = PsiTreeUtil.findElementOfClassAtOffset(file, targetOffset, DartLibraryNameElement.class, false);

      return elementAt;
    }

    return null;
  }

  @Nullable
  public static PsiFile findPsiFile(@NotNull Project project, @NotNull String path) {
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
    if (virtualFile != null) {
      return PsiManager.getInstance(project).findFile(virtualFile);
    }
    return null;
  }

  @NotNull
  private static List<PsiElement> toResult(@Nullable PsiElement element) {
    if (element == null) {
      return Collections.emptyList();
    }
    return new SmartList<PsiElement>(element);
  }

  @Nullable
  private static DartComponent filterAccess(PsiElement element, List<DartComponent> components) {
    final boolean lValue = DartResolveUtil.isLValue(element);
    return ContainerUtil.find(components, new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        if (lValue && component.isSetter()) {
          return true;
        }
        else if (!lValue && component.isGetter()) {
          return true;
        }
        boolean isGetterOrSetter = component.isSetter() || component.isGetter();
        return !isGetterOrSetter;
      }
    });
  }

  /**
   * Find the region with the given offset in the given list of sorted regions.
   * Returns the found region or null.
   */
  @Nullable
  public static DartNavigationRegion findRegion(@NotNull final List<DartServerData.DartNavigationRegion> regions,
                                                final int offset,
                                                final int length) {
    int low = 0;
    int high = regions.size() - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      DartServerData.DartNavigationRegion midVal = regions.get(mid);
      int cmp = midVal.getOffset() - offset;

      if (cmp < 0) {
        low = mid + 1;
      }
      else if (cmp > 0) {
        high = mid - 1;
      }
      else {
        if (midVal.getLength() == length) {
          return midVal;
        }
        return null;
      }
    }
    return null;
  }

  private static List<? extends PsiElement> resolveSimpleReference(@NotNull DartReference reference) {
    final List<? extends PsiElement> result = resolveSimpleReference(reference, reference.getCanonicalText());
    final PsiElement parent = reference.getParent();
    final PsiElement superParent = parent.getParent();
    final boolean isSimpleConstructor = parent instanceof DartType &&
                                        superParent instanceof DartNewExpression &&
                                        ((DartNewExpression)superParent).getReferenceExpression() == null;
    if (!isSimpleConstructor || result.isEmpty()) {
      return result;
    }
    final List<PsiElement> filteredResult = new ArrayList<PsiElement>(result.size());
    for (PsiElement element : result) {
      final PsiElement elementParent = element.getParent();
      if (element instanceof DartComponentName && elementParent instanceof DartClass) {
        final DartComponent component = ((DartClass)elementParent).findNamedConstructor(reference.getCanonicalText());
        if (component != null && DartComponentType.typeOf(component) == DartComponentType.CONSTRUCTOR) {
          filteredResult.add(component.getComponentName());
          continue;
        }
      }
      filteredResult.add(element);
    }
    return filteredResult;
  }

  @NotNull
  public static List<? extends PsiElement> resolveSimpleReference(@NotNull final PsiElement scopeElement, @NotNull final String name) {
    final List<DartComponentName> result = new ArrayList<DartComponentName>();
    // local
    final DartResolveProcessor dartResolveProcessor = new DartResolveProcessor(result, name, DartResolveUtil.isLValue(scopeElement));
    PsiTreeUtil.treeWalkUp(dartResolveProcessor, scopeElement, null, ResolveState.initial());

    // supers
    final DartClass dartClass = PsiTreeUtil.getParentOfType(scopeElement, DartClass.class);
    final boolean inClass = PsiTreeUtil.getParentOfType(scopeElement, DartClassBody.class, false) != null;
    if (result.isEmpty() && dartClass != null && inClass) {
      final DartComponent field = filterAccess(scopeElement, dartClass.findMembersByName(name));
      if (field != null) {
        return toResult(field.getComponentName());
      }
    }

    // global
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFiles = DartResolveUtil.findLibrary(scopeElement.getContainingFile());
      DartResolveUtil.processTopLevelDeclarations(scopeElement, dartResolveProcessor, libraryFiles, name);
    }

    return result;
  }
}
