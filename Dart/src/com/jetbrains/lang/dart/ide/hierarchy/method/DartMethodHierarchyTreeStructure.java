package com.jetbrains.lang.dart.ide.hierarchy.method;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.findDartClass;
import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.getTypeHierarchyItems;

public class DartMethodHierarchyTreeStructure extends HierarchyTreeStructure {
  private final SmartPsiElementPointer myMethod;

  public DartMethodHierarchyTreeStructure(Project project, DartMethodDeclaration element) {
    super(project, null);
    //myBaseDescriptor = buildHierarchyElement(project, element);
    DartClass baseClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
    myBaseDescriptor = new DartMethodHierarchyNodeDescriptor(project, null, baseClass, true, this);
    setBaseElement(myBaseDescriptor);
    ((DartMethodHierarchyNodeDescriptor)myBaseDescriptor).setTreeStructure(this);
    myMethod = SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(element);
  }

  @NotNull
  @Override
  protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor descript) {
    final DartMethodHierarchyNodeDescriptor descriptor = (DartMethodHierarchyNodeDescriptor)descript;
    final DartClass dartClass = descriptor.getType();
    if (dartClass == null) return ArrayUtil.EMPTY_OBJECT_ARRAY;

    final List<TypeHierarchyItem> items = getTypeHierarchyItems(dartClass);
    if (items.isEmpty()) return ArrayUtil.EMPTY_OBJECT_ARRAY;

    addAllVisibleSubclasses(Sets.<TypeHierarchyItem>newHashSet(), myProject, items, items.get(0), descriptor);
    return descriptor.getCachedChildren();
  }

  private void addAllVisibleSubclasses(@NotNull final Set<TypeHierarchyItem> stackItems,
                                       @NotNull final Project project,
                                       @NotNull final List<TypeHierarchyItem> items,
                                       @NotNull final TypeHierarchyItem item,
                                       @NotNull final DartMethodHierarchyNodeDescriptor descriptor) {
    if (!stackItems.add(item)) {
      descriptor.setCachedChildren(ArrayUtil.EMPTY_OBJECT_ARRAY);
      return;
    }
    HierarchyBrowserManager.State state = HierarchyBrowserManager.getInstance(myProject).getState();
    if (state == null) throw new NullPointerException();

    List<HierarchyNodeDescriptor> subDescriptors = Lists.newArrayList();
    try {
      for (int index : item.getSubclasses()) {
        final TypeHierarchyItem subItem = items.get(index);
        final DartClass subclass = findDartClass(project, subItem);
        if (subclass != null) {
          if (state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED) {
            if (shouldHideClass(subclass)) {
              continue;
            }
          }
          final DartMethodHierarchyNodeDescriptor subDescriptor =
            new DartMethodHierarchyNodeDescriptor(project, descriptor, subclass, false, this);
          subDescriptors.add(subDescriptor);
          addAllVisibleSubclasses(stackItems, project, items, subItem, subDescriptor);
        }
      }
    }
    finally {
      stackItems.remove(item);
    }
    descriptor.setCachedChildren(subDescriptors.toArray(new HierarchyNodeDescriptor[subDescriptors.size()]));
  }

  DartMethodDeclaration getBaseMethod() {
    return (DartMethodDeclaration)myMethod.getElement();
  }

  private boolean shouldHideClass(@NotNull DartClass psiClass) {
    //if (getMethod(psiClass, false) != null || isSuperClassForBaseClass(psiClass)) {
    //  return false;
    //}
    //if (hasBaseClassMethod(psiClass) || isAbstract(psiClass)) {
    //  for (final PsiClass subclass : getSubclasses(psiClass)) {
    //    if (!shouldHideClass(subclass)) {
    //      return false;
    //    }
    //  }
    //  return true;
    //}
    return false;
  }

  boolean isSuperClassForBaseClass(final DartClass aClass) {
    DartMethodDeclaration baseMethod = getBaseMethod();
    if (baseMethod == null) {
      return false;
    }
    final DartClass baseClass = PsiTreeUtil.getParentOfType(baseMethod, DartClass.class);
    if (baseClass == null) {
      return false;
    }
    return baseClass.isInheritor(aClass, true);
  }

  //@NotNull
  //private HierarchyNodeDescriptor buildHierarchyElement(final Project project, final DartMethodDeclaration method) {
  //  final DartClass suitableBaseClass = findSuitableBaseClass(method);
  //
  //  HierarchyNodeDescriptor descriptor = null;
  //  final ArrayList<DartClass> superClasses = createSuperClasses(suitableBaseClass);
  //
  //  if (!suitableBaseClass.equals(PsiTreeUtil.getParentOfType(method, DartClass.class))) {
  //    superClasses.add(0, suitableBaseClass);
  //  }
  //
  //  // remove from the top of the branch the classes that contain no 'method'
  //  for(int i = superClasses.size() - 1; i >= 0; i--){
  //    final DartClass psiClass = superClasses.get(i);
  //
  //    if (DartHierarchyUtil.findBaseMethodInClass(method, psiClass, false) == null) {
  //      superClasses.remove(i);
  //    }
  //    else {
  //      break;
  //    }
  //  }
  //
  //  for(int i = superClasses.size() - 1; i >= 0; i--){
  //    final DartClass superClass = superClasses.get(i);
  //    final HierarchyNodeDescriptor newDescriptor = new DartMethodHierarchyNodeDescriptor(project, descriptor, superClass, false, this);
  //    if (descriptor != null){
  //      descriptor.setCachedChildren(new HierarchyNodeDescriptor[] {newDescriptor});
  //    }
  //    descriptor = newDescriptor;
  //  }
  //  final HierarchyNodeDescriptor root = new DartMethodHierarchyNodeDescriptor(project, descriptor, PsiTreeUtil.getParentOfType(method, DartClass.class), true, this);
  //  if (descriptor != null) {
  //    descriptor.setCachedChildren(new HierarchyNodeDescriptor[] {root});
  //  }
  //  return root;
  //}
}
