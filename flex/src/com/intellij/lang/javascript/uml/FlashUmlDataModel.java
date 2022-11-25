// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.*;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.actions.newfile.NewFlexComponentAction;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.index.JSPackageIndexInfo;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public final class FlashUmlDataModel extends DiagramDataModel<Object> {
  private final Map<String, SmartPsiElementPointer<JSClass>> classesAddedByUser = new HashMap<>();
  private final Map<String, SmartPsiElementPointer<JSClass>> classesRemovedByUser = new HashMap<>();
  private final String initialPackage;
  private SmartPsiElementPointer<? extends PsiElement> myInitialElement;
  private final Set<String> packages = new HashSet<>();
  private final Set<String> packagesRemovedByUser = new HashSet<>();

  private final VirtualFile myEditorFile;
  private final SmartPointerManager spManager;

  public FlashUmlDataModel(final Project project, Object element, final VirtualFile file, DiagramProvider<Object> provider) {
    super(project, provider);
    myEditorFile = file;
    spManager = SmartPointerManager.getInstance(project);
    if (element instanceof JSClass) {
      initialPackage = null;
      myInitialElement = spManager.createSmartPsiElementPointer((JSClass)element);
      JSClass psiClass = (JSClass)element;
      classesAddedByUser.put(psiClass.getQualifiedName(), (SmartPsiElementPointer<JSClass>)myInitialElement);
      final Collection<JSClass> classes = JSInheritanceUtil.findAllParentsForClass(psiClass, true);
      for (JSClass aClass : classes) {
        classesAddedByUser.put(aClass.getQualifiedName(), spManager.createSmartPsiElementPointer(aClass));
      }
    }
    else if (element instanceof String) {
      initialPackage = (String)element;

      final GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
      packages.addAll(getSubPackages(initialPackage, searchScope));

      for (JSClass jsClass : getClasses(initialPackage, searchScope)) {
        classesAddedByUser.put(jsClass.getQualifiedName(), spManager.createSmartPsiElementPointer(jsClass));
      }
    }
    else {
      initialPackage = null;
    }
  }

  private static Collection<String> getSubPackages(final String packageName, final GlobalSearchScope searchScope) {
    final Collection<String> result = new HashSet<>();
    JSPackageIndex.processElementsInScope(packageName, null, new JSPackageIndex.PackageElementsProcessor() {
      @Override
      public boolean process(VirtualFile file, @NotNull String name, JSPackageIndexInfo.Kind kind, boolean isPublic) {
        if (kind == JSPackageIndexInfo.Kind.PACKAGE) {
          result.add(StringUtil.getQualifiedName(packageName, name));
        }
        return true;
      }
    }, searchScope, searchScope.getProject());
    return result;
  }

  private static Collection<JSClass> getClasses(final String packageName, final GlobalSearchScope searchScope) {
    final Collection<JSClass> result = new HashSet<>();
    JSPackageIndex.processElementsInScope(packageName, null, new JSPackageIndex.PackageElementsProcessor() {
      @Override
      public boolean process(VirtualFile file, @NotNull String name, JSPackageIndexInfo.Kind kind, boolean isPublic) {
        String qualifiedName = StringUtil.getQualifiedName(packageName, name);
        if (kind == JSPackageIndexInfo.Kind.CLASS || kind == JSPackageIndexInfo.Kind.INTERFACE) {
          PsiElement element = ActionScriptClassResolver.findClassByQNameStatic(qualifiedName, searchScope);
          if (element instanceof JSClass) {
            result.add((JSClass)element);
          }
        }
        return true;
      }
    }, searchScope, searchScope.getProject());
    return result;
  }

  private final Collection<DiagramNode<Object>> myNodes = new HashSet<>();
  private final Collection<DiagramEdge<Object>> myEdges = new HashSet<>();
  private final Collection<DiagramEdge<Object>> myDependencyEdges = new HashSet<>();

  private final Collection<DiagramNode<Object>> myNodesOld = new HashSet<>();
  private final Collection<DiagramEdge<Object>> myEdgesOld = new HashSet<>();
  private final Collection<DiagramEdge<Object>> myDependencyEdgesOld = new HashSet<>();


  @Override
  @NotNull
  public Collection<DiagramNode<Object>> getNodes() {
    return new ArrayList<>(myNodes);
  }

  @Override
  @NotNull
  public Collection<DiagramEdge<Object>> getEdges() {
    if (myDependencyEdges.isEmpty()) {
      return myEdges;
    }
    else {
      Collection<DiagramEdge<Object>> allEdges = new HashSet<>(myEdges);
      allEdges.addAll(myDependencyEdges);
      return allEdges;
    }
  }

  @Override
  @NotNull
  @NonNls
  public String getNodeName(final @NotNull DiagramNode<Object> node) {
    Object element = getIdentifyingElement(node);
    if (element instanceof JSClass) {
      return "Class " + ((JSClass)element).getQualifiedName();
    }
    else if (element instanceof String) {
      return "Package " + element;
    }
    return "";
  }

  @Override
  public void removeNode(@NotNull DiagramNode<Object> node) {
    removeElement(getIdentifyingElement(node));
  }

  @Override
  public void removeEdge(@NotNull DiagramEdge<Object> edge) {
    final Object source = edge.getSource().getIdentifyingElement();
    final Object target = edge.getTarget().getIdentifyingElement();
    final DiagramRelationshipInfo relationship = edge.getRelationship();
    if (!(source instanceof JSClass) || !(target instanceof JSClass) || relationship == DiagramRelationshipInfo.NO_RELATIONSHIP) {
      return;
    }

    final JSClass fromClass = (JSClass)source;
    final JSClass toClass = (JSClass)target;

    if (JSProjectUtil.isInLibrary(fromClass)) {
      return;
    }

    if (fromClass instanceof XmlBackedJSClassImpl && !toClass.isInterface()) {
      Messages.showErrorDialog(fromClass.getProject(), FlexBundle.message("base.component.needed.message"),
                               FlexBundle.message("remove.edge.title"));
      return;
    }

    if (Messages.showYesNoDialog(fromClass.getProject(),
                                 FlexBundle
                                   .message("remove.inheritance.link.prompt", fromClass.getQualifiedName(), toClass.getQualifiedName()),
                                 FlexBundle.message("remove.edge.title"),
                                 Messages.getQuestionIcon()) != Messages.YES) {
      return;
    }

    final Runnable runnable = () -> {
      JSReferenceList refList =
        !fromClass.isInterface() && toClass.isInterface() ? fromClass.getImplementsList() : fromClass.getExtendsList();
      List<FormatFixer> formatters = new ArrayList<>();
      JSRefactoringUtil.removeFromReferenceList(refList, toClass, formatters);
      if (!(fromClass instanceof XmlBackedJSClassImpl) && needsImport(fromClass, toClass)) {
        formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(fromClass.getContainingFile()));
      }
      FormatFixer.fixAll(formatters);
    };

    DiagramAction
      .performCommand(getBuilder(), runnable, FlexBundle.message("remove.relationship.command.name"), null, fromClass.getContainingFile());
  }

  private static boolean needsImport(JSClass context, JSClass referenced) {
    String packageName = StringUtil.getPackageName(referenced.getQualifiedName());
    return !packageName.isEmpty() && !packageName.equals(StringUtil.getPackageName(context.getQualifiedName()));
  }

  @Override
  public void refreshDataModel() {
    clearAll();
    updateDataModel();
  }

  @NotNull
  @Override
  public ModificationTracker getModificationTracker() {
    return PsiManager.getInstance(getProject()).getModificationTracker();
  }

  private void clearAll() {
    clearAndBackup(myNodes, myNodesOld);
    clearAndBackup(myEdges, myEdgesOld);
    clearAndBackup(myDependencyEdges, myDependencyEdgesOld);
  }

  public void removeAllElements() {
    classesRemovedByUser.clear();
    classesRemovedByUser.putAll(classesAddedByUser);
    classesAddedByUser.clear();
    packagesRemovedByUser.clear();
    packagesRemovedByUser.addAll(packages);
    packages.clear();
    clearAll();
  }

  private boolean isAllowedToShow(JSClass psiClass) {
    if (psiClass == null || !psiClass.isValid()) return false;

    final DiagramScopeManager<Object> scopeManager = getScopeManager();
    if (scopeManager != null && !scopeManager.contains(psiClass)) return false;

    if (isInsidePackages(psiClass)) return false;
    return true;
  }

  public synchronized void updateDataModel() {
    final Set<JSClass> classes = getAllClasses();
    syncPackages();
    final Set<JSClass> interfaces = new HashSet<>();

    for (String psiPackage : packages) {

      if (FlashUmlElementManager.packageExists(getProject(), psiPackage, GlobalSearchScope.allScope(getProject()))) {
        myNodes.add(new FlashUmlPackageNode(psiPackage, getProvider()));
      }
    }
    for (JSClass psiClass : classes) {
      if (isAllowedToShow(psiClass)) {
        myNodes.add(new FlashUmlClassNode(psiClass, getProvider()));
      }

      if (psiClass.isInterface()) {
        interfaces.add(psiClass);
      }
    }

    for (JSClass psiClass : classes) {
      {
        DiagramNode<Object> source = findNode(psiClass);
        DiagramNode<Object> target = null;
        Collection<JSClass> processed = new ArrayList<>();
        JSClass superClass = getSuperClass(psiClass, processed);
        while (target == null && superClass != null) {
          target = findNode(superClass);
          superClass = getSuperClass(superClass, processed);
        }

        if (source != null && target != null && source != target) {
          if (!((JSClass)getIdentifyingElement(source)).isInterface() ||
              !JSResolveUtil.isObjectClass((JSClass)getIdentifyingElement(target))) {
            addEdge(source, target,
                    psiClass.isInterface() ? FlashUmlRelationship.INTERFACE_GENERALIZATION : FlashUmlRelationship.GENERALIZATION);
          }
        }
      }

      for (JSClass inter : psiClass.getImplementedInterfaces()) {
        if (interfaces.contains(inter)) {
          DiagramNode<Object> source = findNode(psiClass);
          DiagramNode<Object> target = findNode(inter);
          if (source != null && target != null && source != target) {
            addEdge(source, target, FlashUmlRelationship.REALIZATION);
          }
        }
      }
      if (psiClass.isInterface()) {
        Set<JSClass> found = new HashSet<>();
        findNearestInterfaces(psiClass, found);

        for (JSClass inter : found) {
          if (interfaces.contains(inter)) {
            DiagramNode<Object> source = findNode(psiClass);
            DiagramNode<Object> target = findNode(inter);
            if (source != null && target != null && source != target) {
              addEdge(source, target, FlashUmlRelationship.INTERFACE_GENERALIZATION);
            }
          }
        }
      }
      else {
        //Collect all realized interfaces
        Set<JSClass> inters = new HashSet<>();
        ContainerUtil.addAll(inters, psiClass.getImplementedInterfaces());
        Collection<JSClass> processed = new ArrayList<>();
        JSClass cur = getSuperClass(psiClass, processed);
        while (cur != null) {
          if (findNode(cur) == null) {
            ContainerUtil.addAll(inters, cur.getImplementedInterfaces());
          }
          else {
            break;
          }
          cur = getSuperClass(cur, processed);
        }

        ArrayList<JSClass> faces = new ArrayList<>(inters);

        while (!faces.isEmpty()) {
          JSClass inter = faces.get(0);
          if (findNode(inter) != null) {
            DiagramNode<Object> source = findNode(psiClass);
            DiagramNode<Object> target = findNode(inter);
            if (source != null && target != null && source != target) {
              addEdge(source, target, FlashUmlRelationship.REALIZATION);
            }
            faces.remove(inter);
          }
          else {
            faces.remove(inter);
            ContainerUtil.addAll(faces, inter.getImplementedInterfaces());
          }
        }
      }
    }

    if (isShowDependencies()) {
      final EnumSet<FlashUmlDependenciesSettingsOption> options = FlashUmlDependenciesSettingsOption.getEnabled();
      for (JSClass psiClass : classes) {
        showDependenciesFor(psiClass, options);
      }
    }
    //merge!
    mergeWithBackup(myNodes, myNodesOld);
    mergeWithBackup(myEdges, myEdgesOld);
    mergeWithBackup(myDependencyEdges, myDependencyEdgesOld);
  }

  private void showDependenciesFor(final JSClass clazz, final EnumSet<FlashUmlDependenciesSettingsOption> options) {
    DiagramNode<Object> mainNode = findNode(clazz);
    if (mainNode == null) return;

    FlashUmlDependencyProvider provider = new FlashUmlDependencyProvider(clazz);

    Collection<Pair<JSClass, FlashUmlRelationship>> list = provider.computeUsedClasses();
    for (Pair<JSClass, FlashUmlRelationship> pair : list) {
      if (shouldShow(options, clazz, pair.first, pair.second)) {
        DiagramNode<Object> node = findNode(pair.first);
        if (node != null) {
          addDependencyEdge(mainNode, node, pair.second);
        }
      }
    }
  }

  private static boolean shouldShow(EnumSet<FlashUmlDependenciesSettingsOption> options,
                                    final JSClass from,
                                    final JSClass to,
                                    final FlashUmlRelationship relShip) {
    if (JSResolveUtil.isObjectClass(from) && JSResolveUtil.isObjectClass(to)) {
      return false;
    }
    if (!options.contains(FlashUmlDependenciesSettingsOption.SELF) && JSPsiImplUtils.isTheSameClass(from, to)) {
      return false;
    }
    if (!options.contains(FlashUmlDependenciesSettingsOption.ONE_TO_ONE) && relShip.getType() == FlashUmlRelationship.TYPE_ONE_TO_ONE) {
      return false;
    }
    if (!options.contains(FlashUmlDependenciesSettingsOption.ONE_TO_MANY) && relShip.getType() == FlashUmlRelationship.TYPE_ONE_TO_MANY) {
      return false;
    }
    if (!options.contains(FlashUmlDependenciesSettingsOption.USAGES) && relShip.getType() == FlashUmlRelationship.TYPE_DEPENDENCY) {
      return false;
    }
    if (!options.contains(FlashUmlDependenciesSettingsOption.CREATE) && relShip.getType() == FlashUmlRelationship.TYPE_CREATE) {
      return false;
    }
    return true;
  }

  @Nullable
  private static JSClass getSuperClass(JSClass psiClass, Collection<JSClass> processed) {
    JSClass[] superClasses = psiClass.getSuperClasses();
    if (superClasses.length > 0 &&
        !superClasses[0].isEquivalentTo(psiClass) &&
        !JSPsiImplUtils.containsEquivalent(processed, superClasses[0])) {
      processed.add(superClasses[0]);
      return superClasses[0];
    }
    return null;
  }

  private static <T> void clearAndBackup(Collection<T> target, Collection<T> backup) {
    backup.clear();
    backup.addAll(target);
    target.clear();
  }

  private static <T> void mergeWithBackup(Collection<T> target, Collection<T> backup) {
    for (T t : backup) {
      if (target.contains(t)) {
        target.remove(t);
        target.add(t);
      }
    }
  }

  private void syncPackages() {
    final GlobalSearchScope searchScope = GlobalSearchScope.allScope(getProject());
    if (initialPackage == null || FlashUmlElementManager.packageExists(getProject(), initialPackage, searchScope)) return;

    final Set<String> psiPackages = new HashSet<>(getSubPackages(initialPackage, searchScope));
    for (String fqn : packages) psiPackages.remove(fqn);
    for (String fqn : packagesRemovedByUser) psiPackages.remove(fqn);

    if (!psiPackages.isEmpty()) {
      packages.addAll(psiPackages);
    }
  }

  private static void findNearestInterfaces(final JSClass psiClass, final Set<JSClass> result) {
    for (JSClass anInterface : psiClass.getSuperClasses()) {
      if (result.contains(anInterface)) {
        continue; // don't check isEquivalent, equality check is enough for interfaces
      }
      result.add(anInterface);
      findNearestInterfaces(anInterface, result);
    }
  }

  private static boolean isGeneralizationEdgeAllowed(final JSClass psiClass) {
    return !psiClass.isInterface();
  }

  private boolean isInsidePackages(JSClass psiClass) {
    return packages.contains(StringUtil.getPackageName(psiClass.getQualifiedName()));
  }

  public FlashUmlEdge addEdge(DiagramNode<Object> from, DiagramNode<Object> to, DiagramRelationshipInfo relationship) {
    return addEdge(from, to, relationship, myEdges);
  }

  public FlashUmlEdge addDependencyEdge(DiagramNode<Object> from, DiagramNode<Object> to, DiagramRelationshipInfo relationship) {
    return addEdge(from, to, relationship, myDependencyEdges);
  }

  private static FlashUmlEdge addEdge(DiagramNode<Object> from,
                                   DiagramNode<Object> to,
                                   DiagramRelationshipInfo relationship,
                                   Collection<DiagramEdge<Object>> storage) {
    for (DiagramEdge edge : storage) {
      if (edge.getSource() == from && edge.getTarget() == to && relationship.equals(edge.getRelationship())) return null;
    }
    FlashUmlEdge result = new FlashUmlEdge(from, to, relationship);
    storage.add(result);
    return result;
  }

  private Set<JSClass> getAllClasses() {
    Set<JSClass> classes = new HashSet<>();
    for (SmartPsiElementPointer<JSClass> pointer : classesAddedByUser.values()) {
      classes.add(pointer.getElement());
    }
    final GlobalSearchScope searchScope = GlobalSearchScope.allScope(getProject());
    if (initialPackage != null && FlashUmlElementManager.packageExists(getProject(), initialPackage, searchScope)) {
      classes.addAll(getClasses(initialPackage, searchScope));
    }
    for (String psiPackage : packages) {
      if (FlashUmlElementManager.packageExists(getProject(), psiPackage, searchScope)) {
        classes.addAll(getClasses(psiPackage, searchScope));
      }
    }
    classes.remove(null);
    Set<JSClass> temp = new HashSet<>();

    for (JSClass aClass : classes) {
      if (!aClass.isValid()) temp.add(aClass);
    }

    for (SmartPsiElementPointer<JSClass> cls : classesRemovedByUser.values()) {
      classes.remove(cls.getElement());
    }
    classes.removeAll(temp);
    return classes;
  }

  @Nullable
  public DiagramNode<Object> findNode(Object object) {
    String objectFqn = getFqn(object);
    for (DiagramNode<Object> node : getNodes()) {
      final String fqn = getFqn(getIdentifyingElement(node));
      if (fqn != null && fqn.equals(objectFqn)) {
        if (object instanceof JSClass && !(node instanceof FlashUmlClassNode)) continue;
        if (object instanceof String && !(node instanceof FlashUmlPackageNode)) continue;
        return node;
      }
    }
    //final SmartPsiElementPointer<JSPackage> ptr = packages.get(UmlUtils.getPackageName(psiElement));
    return null; //ptr == null ? null : findNode(ptr.getElement());
  }

  @Nullable
  private static String getFqn(Object element) {
    if (element instanceof JSQualifiedNamedElement) {
      String qName = ((JSQualifiedNamedElement)element).getQualifiedName();
      return qName != null ? FlashUmlVfsResolver.fixVectorTypeName(qName) : null;
    }
    if (element instanceof String) {
      return (String)element;
    }
    return null;
  }

  public boolean contains(PsiElement psiElement) {
    return findNode(psiElement) != null;
  }

  @Override
  public void dispose() {
  }

  public void removeElement(final Object element) {
    DiagramNode node = findNode(element);
    if (node == null) {
      classesAddedByUser.remove(getFqn(element));
      return;
    }

    Collection<DiagramEdge> edgesToRemove = new ArrayList<>();
    for (DiagramEdge edge : myEdges) {
      if (node.equals(edge.getTarget()) || node.equals(edge.getSource())) {
        edgesToRemove.add(edge);
      }
    }
    myEdges.removeAll(edgesToRemove);

    Collection<DiagramEdge> dependencyEdgesToRemove = new ArrayList<>();
    for (DiagramEdge edge : myDependencyEdges) {
      if (node.equals(edge.getTarget()) || node.equals(edge.getSource())) {
        dependencyEdgesToRemove.add(edge);
      }
    }
    myDependencyEdges.removeAll(dependencyEdgesToRemove);


    myNodes.remove(node);
    if (element instanceof JSClass) {
      final JSClass psiClass = (JSClass)element;
      classesRemovedByUser.put(psiClass.getQualifiedName(), spManager.createSmartPsiElementPointer(psiClass));
      classesAddedByUser.remove(psiClass.getQualifiedName());
    }
    if (element instanceof String) {
      String p = (String)element;
      packages.remove(p);
      packagesRemovedByUser.add(p);

      Set<String> toDelete = new HashSet<>();
      for (String key : classesAddedByUser.keySet()) {
        final SmartPsiElementPointer<JSClass> pointer = classesAddedByUser.get(key);
        final JSClass psiClass = pointer.getElement();
        if (p.equals(StringUtil.getPackageName(psiClass.getQualifiedName()))) {
          toDelete.add(key);
        }
      }
      for (String key : toDelete) {
        classesAddedByUser.remove(key);
      }
    }
  }

  @Override
  @Nullable
  public DiagramNode<Object> addElement(@Nullable Object element) {
    if (findNode(element) != null) return null;

    if (element instanceof JSClass) {
      if (!isAllowedToShow((JSClass)element)) {
        return null;
      }

      JSClass psiClass = (JSClass)element;
      if (psiClass.getQualifiedName() == null) return null;
      final SmartPsiElementPointer<JSClass> pointer = spManager.createSmartPsiElementPointer(psiClass);
      final String fqn = psiClass.getQualifiedName();
      classesAddedByUser.put(fqn, pointer);
      classesRemovedByUser.remove(fqn);

      setupScopeManager(psiClass, true);

      return new FlashUmlClassNode((JSClass)element, getProvider());
    }
    else if (element instanceof String) {
      String aPackage = (String)element;
      packages.add(aPackage);
      packagesRemovedByUser.remove(aPackage);
      return new FlashUmlPackageNode(aPackage, getProvider());
    }
    return null;
  }


  @Override
  public void expandNode(final @NotNull DiagramNode<Object> node) {
    final Object element = node.getIdentifyingElement();
    if (element instanceof String) {
      expandPackage((String)element);
    }
  }

  public void expandPackage(final String psiPackage) {
    packages.remove(psiPackage);
    packagesRemovedByUser.add(psiPackage);
    final GlobalSearchScope searchScope = GlobalSearchScope.allScope(getProject());
    for (JSClass psiClass : getClasses(psiPackage, searchScope)) {
      addElement(psiClass);
    }
    for (String aPackage : getSubPackages(psiPackage, searchScope)) {
      addElement(aPackage);
    }
  }

  @Override
  public void collapseNode(final @NotNull DiagramNode<Object> node) {
    Object element = node.getIdentifyingElement();
    String fqn = getFqn(element);
    if (fqn == null) {
      return;
    }

    String parentPackage = StringUtil.getPackageName(fqn);
    if (parentPackage.isEmpty()) {
      return;
    }

    final String fqnStart = parentPackage + ".";
    final ArrayList<String> toRemove = new ArrayList<>();
    for (String p : packages) {
      if (p.startsWith(fqnStart)) {
        toRemove.add(p);
      }
    }
    packages.removeAll(toRemove);
    toRemove.clear();

    for (String s : classesAddedByUser.keySet()) {
      if (s.startsWith(fqnStart)) {
        toRemove.add(s);
      }
    }
    for (String s : toRemove) {
      classesAddedByUser.remove(s);
    }
    packages.add(parentPackage);
    packagesRemovedByUser.remove(parentPackage);
  }

  List<String> getAllClassesFQN() {
    List<String> fqns = new ArrayList<>();
    for (DiagramNode node : getNodes()) {
      final Object identifyingElement = getIdentifyingElement(node);
      if (identifyingElement instanceof JSClass) {
        fqns.add(((JSClass)identifyingElement).getQualifiedName());
      }
    }
    return fqns;
  }

  List<String> getAllPackagesFQN() {
    List<String> fqns = new ArrayList<>();
    for (DiagramNode node : getNodes()) {
      final Object identifyingElement = getIdentifyingElement(node);
      if (identifyingElement instanceof JSPackage) {
        fqns.add(((JSPackage)identifyingElement).getQualifiedName());
      }
    }
    return fqns;
  }

  @Nullable
  public PsiElement getInitialElement() {
    if (myInitialElement == null) return null;
    final PsiElement element = myInitialElement.getElement();
    return element == null || !element.isValid() ? null : element;
  }

  public String getInitialPackage() {
    return initialPackage;
  }

  public boolean hasNotValid() {
    for (DiagramNode<Object> node : getNodes()) {
      if (!isValid(getIdentifyingElement(node))) {
        return true;
      }
    }
    return false;
  }

  private static boolean isValid(Object element) {
    if (element instanceof PsiElement) return ((PsiElement)element).isValid();
    return false;
  }

  public static String getMessage(final JSClass source, final JSClass target, final DiagramRelationshipInfo relationship) {
    if (relationship == FlashUmlRelationship.ANNOTATION) {
      return FlexBundle.message("remove.annotation.from.class");
    }
    else {
      return FlexBundle.message("this.will.remove.relationship.link.between.classes");
    }
  }

  public VirtualFile getFile() {
    return myEditorFile;
  }

  @Override
  public boolean hasElement(@Nullable Object element) {
    return findNode(element) != null;
  }

  @Override
  public boolean isPsiListener() {
    return true;
  }

  @Nullable
  public static Object getIdentifyingElement(DiagramNode node) {
    if (node instanceof FlashUmlClassNode || node instanceof FlashUmlPackageNode) {
      return node.getIdentifyingElement();
    }
    if (node instanceof DiagramNoteNode) {
      final DiagramNode delegate = ((DiagramNoteNode)node).getIdentifyingElement();
      if (delegate != node) {
        return getIdentifyingElement(delegate);
      }
    }
    return null;
  }

  @Override
  @Nullable
  public DiagramEdge<Object> createEdge(@NotNull final DiagramNode<Object> from, @NotNull final DiagramNode<Object> to) {
    final JSClass fromClass = (JSClass)from.getIdentifyingElement();
    final JSClass toClass = (JSClass)to.getIdentifyingElement();

    if (fromClass.isEquivalentTo(toClass)) {
      return null;
    }

    if (toClass.isInterface()) {
      if (JSPsiImplUtils.containsEquivalent(fromClass.isInterface() ?
                                            fromClass.getSuperClasses() : fromClass.getImplementedInterfaces(), toClass)) {
        return null;
      }

      Callable<DiagramEdge<Object>> callable = () -> {
        String targetQName = toClass.getQualifiedName();
        JSRefactoringUtil.addToSupersList(fromClass, targetQName, true);
        if (targetQName.contains(".") && !(fromClass instanceof XmlBackedJSClassImpl)) {
          List<FormatFixer> formatters = new ArrayList<>();
          formatters.add(ImportUtils.insertImportStatements(fromClass, Collections.singletonList(targetQName)));
          formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(fromClass.getContainingFile()));
          FormatFixer.fixAll(formatters);
        }
        return addEdgeAndRefresh(from, to, fromClass.isInterface()
                                           ? FlashUmlRelationship.GENERALIZATION
                                           : FlashUmlRelationship.INTERFACE_GENERALIZATION);
      };
      String commandName =
        FlexBundle
          .message(fromClass.isInterface() ? "create.extends.relationship.command.name" : "create.implements.relationship.command.name",
                   fromClass.getQualifiedName(), toClass.getQualifiedName());
      return DiagramAction.performCommand(getBuilder(), callable, commandName, null, fromClass.getContainingFile());
    }
    else {
      if (fromClass.isInterface()) {
        return null;
      }
      else if (fromClass instanceof XmlBackedJSClassImpl) {
        JSClass[] superClasses = fromClass.getSuperClasses();
        if (JSPsiImplUtils.containsEquivalent(superClasses, toClass)) {
          return null;
        }

        if (superClasses.length > 0) { // if base component is not resolved, replace it silently
          final JSClass currentParent = superClasses[0];
          if (MessageDialogBuilder.yesNo(FlexBundle.message("create.edge.title"), FlexBundle
            .message("replace.base.component.prompt", currentParent.getQualifiedName(), toClass.getQualifiedName())).show() == Messages.NO) {
            return null;
          }
        }
        Callable<DiagramEdge<Object>> callable = () -> {
          NewFlexComponentAction.setParentComponent((MxmlJSClass)fromClass, toClass.getQualifiedName());
          return addEdgeAndRefresh(from, to, DiagramRelationships.GENERALIZATION);
        };
        String commandName =
          FlexBundle.message("create.extends.relationship.command.name", fromClass.getQualifiedName(), toClass.getQualifiedName());
        return DiagramAction.performCommand(getBuilder(), callable, commandName, null, fromClass.getContainingFile());
      }
      else {
        final JSClass[] superClasses = fromClass.getSuperClasses();
        if (JSPsiImplUtils.containsEquivalent(superClasses, toClass)) {
          return null;
        }

        if (superClasses.length > 0 &&
            !JSResolveUtil.isObjectClass(superClasses[0])) { // if base class is not resolved, replace it silently
          final JSClass currentParent = superClasses[0];
          if (MessageDialogBuilder.yesNo(FlexBundle.message("create.edge.title"), FlexBundle
            .message("replace.base.class.prompt", currentParent.getQualifiedName(), toClass.getQualifiedName()))
                .icon(Messages.getQuestionIcon()).show() == Messages.NO) {
            return null;
          }
        }
        Callable<DiagramEdge<Object>> callable = () -> {
          List<FormatFixer> formatters = new ArrayList<>();
          boolean optimize = false;
          if (superClasses.length > 0 && !JSResolveUtil.isObjectClass(superClasses[0])) {
            JSRefactoringUtil.removeFromReferenceList(fromClass.getExtendsList(), superClasses[0], formatters);
            optimize = needsImport(fromClass, superClasses[0]);
          }
          JSRefactoringUtil.addToSupersList(fromClass, toClass.getQualifiedName(), false);
          if (needsImport(fromClass, toClass)) {
            formatters.add(ImportUtils.insertImportStatements(fromClass, Collections.singletonList(toClass.getQualifiedName())));
            optimize = true;
          }
          if (optimize) {
            formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(fromClass.getContainingFile()));
          }
          FormatFixer.fixAll(formatters);
          return addEdgeAndRefresh(from, to, DiagramRelationships.GENERALIZATION);
        };
        String commandName =
          FlexBundle.message("create.extends.relationship.command.name", fromClass.getQualifiedName(), toClass.getQualifiedName());
        return DiagramAction.performCommand(getBuilder(), callable, commandName, null, fromClass.getContainingFile());
      }
    }
  }

  private DiagramEdge<Object> addEdgeAndRefresh(DiagramNode<Object> from, DiagramNode<Object> to, DiagramRelationshipInfo type) {
    FlashUmlEdge result = addEdge(from, to, type);
    final DiagramBuilder builder = getBuilder();
    builder.queryUpdate().withDataReload().withPresentationUpdate().run();
    return result;
  }

  @Override
  public boolean isDependencyDiagramSupported() {
    return true;
  }
}
