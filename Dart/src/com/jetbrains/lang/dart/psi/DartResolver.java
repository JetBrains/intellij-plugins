package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.ResolveScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartResolver implements ResolveCache.AbstractResolver<DartReference, List<? extends PsiElement>> {
  public static final DartResolver INSTANCE = new DartResolver();

  @Nullable
  @Override
  public List<? extends PsiElement> resolve(@NotNull DartReference reference, boolean incompleteCode) {
    if (reference instanceof DartThisExpression) {
      return toResult(PsiTreeUtil.getParentOfType(reference, DartClass.class));
    }
    if (reference instanceof DartParameterNameReferenceExpression) {
      final DartCallExpression callExpression = PsiTreeUtil.getParentOfType(reference, DartCallExpression.class);
      final DartExpression expression = callExpression != null ? callExpression.getExpression() : null;
      final PsiElement target = expression instanceof DartReference ? ((DartReference)expression).resolve() : null;
      final DartFormalParameterList parameters =
        PsiTreeUtil.getChildOfType(target != null ? target.getParent() : null, DartFormalParameterList.class);
      return toResult(DartResolveUtil.findParameterByName(parameters, reference.getText()));
    }
    if (DartResolveUtil.aloneOrFirstInChain(reference)) {
      return resolveSimpleReference(reference);
    }
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);
    // reference [node, node]
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(reference, DartReference.class);
    if (references != null && references.length == 2) {
      // prefix
      final List<DartComponentName> names = DartResolveUtil
        .findComponentsInLibraryByPrefix(reference, references[0].getCanonicalText(),
                                         references[1].getCanonicalText());
      if (!names.isEmpty()) {
        return toResult(names);
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
      // prefix
      final List<DartComponentName> names = DartResolveUtil
        .findComponentsInLibraryByPrefix(reference, leftReference.getCanonicalText(), reference.getCanonicalText());
      if (!names.isEmpty()) {
        return toResult(names);
      }
    }

    return null;
  }

  private static List<PsiElement> toResult(List<? extends PsiElement> elements) {
    return ContainerUtil.filter(elements, new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement element) {
        return element != null;
      }
    });
  }

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

  private static List<? extends PsiElement> resolveSimpleReference(@NotNull DartReference reference) {
    final List<? extends PsiElement> result = resolveSimpleReference(reference, reference.getCanonicalText());
    final PsiElement parent = reference.getParent();
    final PsiElement superParent = parent.getParent();
    final boolean isSimpleConstructor = parent instanceof DartType
                                        && superParent instanceof DartNewExpression
                                        && ((DartNewExpression)superParent).getReferenceExpression() == null;
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

  public static List<? extends PsiElement> resolveSimpleReference(PsiElement scopeElement, String name) {
    final List<DartComponentName> result = new ArrayList<DartComponentName>();
    // local
    final ResolveScopeProcessor resolveScopeProcessor =
      new ResolveScopeProcessor(result, name, DartResolveUtil.isLValue(scopeElement));
    PsiTreeUtil.treeWalkUp(resolveScopeProcessor, scopeElement, null, new ResolveState());
    // supers
    final DartClass dartClass = PsiTreeUtil.getParentOfType(scopeElement, DartClass.class);
    final boolean inClass = PsiTreeUtil.getParentOfType(scopeElement, DartClassBody.class, false) != null ||
                            PsiTreeUtil.getParentOfType(scopeElement, DartInterfaceBody.class, false) != null;
    if (result.isEmpty() && dartClass != null && inClass) {
      final DartComponent field = filterAccess(scopeElement, dartClass.findMembersByName(name));
      if (field != null) {
        return toResult(field.getComponentName());
      }
    }
    // global
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFiles = DartResolveUtil.findLibrary(scopeElement.getContainingFile());
      DartResolveUtil.processTopLevelDeclarations(scopeElement, resolveScopeProcessor, libraryFiles, name);
    }
    // dart:core
    if (result.isEmpty() && !"void".equals(name)) {
      final List<VirtualFile> libraryFiles = DartLibraryIndex.findLibraryClass(scopeElement, "dart:core");
      DartResolveUtil.processTopLevelDeclarations(scopeElement, resolveScopeProcessor, libraryFiles, name);
    }

    return result;
  }
}
