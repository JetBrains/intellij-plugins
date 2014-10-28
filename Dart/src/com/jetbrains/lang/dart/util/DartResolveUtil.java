package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.BooleanValueHolder;
import com.intellij.util.Function;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.ContainerUtilRt;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.ide.index.*;
import com.jetbrains.lang.dart.ide.info.DartFunctionDescription;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.AbstractDartPsiClass;
import com.jetbrains.lang.dart.psi.impl.DartPsiCompositeElementImpl;
import com.jetbrains.lang.dart.resolve.DartPsiScopeProcessor;
import com.jetbrains.lang.dart.resolve.DartResolveProcessor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.jetbrains.lang.dart.ide.index.DartImportOrExportInfo.Kind;
import static com.jetbrains.lang.dart.util.DartUrlResolver.*;

public class DartResolveUtil {

  public static final String OBJECT = "Object";

  public static List<PsiElement> findDartRoots(PsiFile psiFile) {
    if (psiFile instanceof XmlFile) {
      return findDartRootsInXml((XmlFile)psiFile);
    }
    return psiFile instanceof DartFile ? Collections.<PsiElement>singletonList(psiFile) : Collections.<PsiElement>emptyList();
  }

  private static List<PsiElement> findDartRootsInXml(XmlFile xmlFile) {
    final List<PsiElement> result = new ArrayList<PsiElement>();
    xmlFile.acceptChildren(new XmlRecursiveElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        if (element instanceof DartEmbeddedContent) {
          result.add(element);
          return;
        }
        super.visitElement(element);
      }
    });
    return result;
  }

  public static boolean isLValue(PsiElement element) {
    if (element instanceof PsiFile) return false;
    PsiElement nextSibling = UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(element);
    while (nextSibling == null && element != null) {
      element = element.getParent();
      nextSibling = UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(element);
    }
    if (nextSibling instanceof LeafPsiElement) {
      return DartTokenTypesSets.ASSIGNMENT_OPERATORS.contains(((LeafPsiElement)nextSibling).getElementType());
    }
    return nextSibling instanceof DartAssignmentOperator;
  }

  public static boolean checkParametersType(DartFormalParameterList list, DartClass... classes) {
    final List<DartNormalFormalParameter> normalFormalParameterList = list.getNormalFormalParameterList();
    int i = 0;
    for (int size = normalFormalParameterList.size(); i < size; i++) {
      if (i >= classes.length) return false;
      final DartNormalFormalParameter normalFormalParameter = normalFormalParameterList.get(i);
      final DartType dartType = findType(normalFormalParameter);
      if (dartType != null && !canAssign(resolveClassByType(dartType).getDartClass(), classes[i])) {
        return false;
      }
    }
    final DartNamedFormalParameters namedFormalParameters = list.getNamedFormalParameters();
    if (namedFormalParameters == null) {
      return true;
    }
    for (int size = namedFormalParameters.getDefaultFormalNamedParameterList().size(); i - normalFormalParameterList.size() < size; ++i) {
      final DartDefaultFormalNamedParameter defaultFormalParameter =
        namedFormalParameters.getDefaultFormalNamedParameterList().get(i - normalFormalParameterList.size());
      final DartType dartType = findType(defaultFormalParameter.getNormalFormalParameter());
      if (dartType != null && !canAssign(resolveClassByType(dartType).getDartClass(), classes[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean canAssign(@Nullable final DartClass baseClass, @Nullable DartClass aClass) {
    if (baseClass == null || aClass == null) {
      return true;
    }
    final BooleanValueHolder result = new BooleanValueHolder(false);
    processSuperClasses(new PsiElementProcessor<DartClass>() {
      @Override
      public boolean execute(@NotNull DartClass dartClass) {
        if (dartClass == baseClass) {
          result.setValue(true);
          return false;
        }
        return true;
      }
    }, aClass);
    return result.getValue();
  }

  @Nullable
  public static DartType findType(@Nullable PsiElement element) {
    if (element instanceof DartDefaultFormalNamedParameter) {
      return findType(((DartDefaultFormalNamedParameter)element).getNormalFormalParameter());
    }
    if (element instanceof DartNormalFormalParameter) {
      final DartFunctionSignature functionSignature = ((DartNormalFormalParameter)element).getFunctionSignature();
      final DartFieldFormalParameter fieldFormalParameter = ((DartNormalFormalParameter)element).getFieldFormalParameter();
      final DartSimpleFormalParameter simpleFormalParameter = ((DartNormalFormalParameter)element).getSimpleFormalParameter();

      if (functionSignature != null) {
        // todo return some FUNCTION type?
      }
      if (fieldFormalParameter != null) return fieldFormalParameter.getType();
      if (simpleFormalParameter != null) return simpleFormalParameter.getType();
    }
    return null;
  }

  @NotNull
  public static DartClassResolveResult findCoreClass(PsiElement context, String className) {
    final List<VirtualFile> libraryFile = DartLibraryIndex.findLibraryClass(context, "dart:core");
    final List<DartComponentName> result = new ArrayList<DartComponentName>();
    processTopLevelDeclarations(context, new DartResolveProcessor(result, className), libraryFile, className);
    final PsiElement parent = result.isEmpty() ? null : result.iterator().next().getParent();
    return DartClassResolveResult.create(parent instanceof DartClass ? (DartClass)parent : null);
  }

  public static List<DartClass> findClassesByParent(@NotNull final DartClass superClass, @Nullable PsiElement context) {
    if (context == null) {
      return Collections.emptyList();
    }
    final DartClass[] classesInFile = PsiTreeUtil.getChildrenOfType(context, DartClass.class);
    if (classesInFile == null) {
      return Collections.emptyList();
    }
    return ContainerUtil.filter(classesInFile, new Condition<DartClass>() {
      @Override
      public boolean value(DartClass dartClass) {
        return checkInheritanceBySuperAndImplementationList(dartClass, superClass);
      }
    });
  }

  private static boolean checkInheritanceBySuperAndImplementationList(DartClass dartClass, DartClass superCandidate) {
    final DartType superClass = dartClass.getSuperClass();
    if (superClass == null && OBJECT.equals(superCandidate.getName())) return true;
    if (typeResolvesToClass(superClass, superCandidate)) return true;
    for (DartType dartType : getImplementsAndMixinsList(dartClass)) {
      if (typeResolvesToClass(dartType, superCandidate)) return true;
    }
    return false;
  }

  public static boolean typeResolvesToClass(@Nullable DartType dartType, DartClass classCandidate) {
    if (dartType == null) {
      return false;
    }
    final String typeName = dartType.getReferenceExpression().getText();
    if (!typeName.equals(classCandidate.getName())) {
      return false;
    }
    return resolveClassByType(dartType).getDartClass() == classCandidate;
  }

  @NotNull
  public static String getLibraryName(@NotNull final PsiFile psiFile) {
    for (PsiElement root : findDartRoots(psiFile)) {
      final DartLibraryStatement libraryStatement = PsiTreeUtil.getChildOfType(root, DartLibraryStatement.class);
      if (libraryStatement != null) {
        return libraryStatement.getLibraryName();
      }

      final DartPartOfStatement partOfStatement = PsiTreeUtil.getChildOfType(root, DartPartOfStatement.class);
      if (partOfStatement != null) {
        return partOfStatement.getLibraryName();
      }
    }

    return psiFile.getName();
  }

  @NotNull
  public static List<DartComponentName> findComponentsInLibraryByPrefix(PsiElement contextToSearch,
                                                                        String libraryPrefix,
                                                                        String componentName) {
    final VirtualFile virtualFile = getImportedFileByImportPrefix(contextToSearch.getContainingFile(), libraryPrefix);
    if (virtualFile != null) {
      final List<DartComponentName> result = new ArrayList<DartComponentName>();
      processTopLevelDeclarations(contextToSearch, new DartResolveProcessor(result, componentName),
                                  virtualFile, componentName);
      return result;
    }
    return Collections.emptyList();
  }

  public static Set<DartClass> getClassDeclarations(PsiElement context) {
    final DartComponent[] components = PsiTreeUtil.getChildrenOfType(context, DartComponent.class);
    if (components == null) {
      return Collections.emptySet();
    }
    final Set<DartClass> result = new THashSet<DartClass>();
    for (DartComponent component : components) {
      final DartComponentType type = DartComponentType.typeOf(component);
      if (type == DartComponentType.CLASS) {
        result.add((DartClass)component);
      }
    }
    return result;
  }

  @Nullable
  public static VirtualFile getImportedFileByImportPrefix(final @NotNull PsiElement context, final @NotNull String prefix) {
    final Project project = context.getProject();
    final List<VirtualFile> virtualFiles = findLibrary(context.getContainingFile());
    for (VirtualFile virtualFile : virtualFiles) {
      for (DartImportOrExportInfo importOrExportInfo : DartImportAndExportIndex.getImportAndExportInfos(project, virtualFile)) {
        if (importOrExportInfo.getKind() != Kind.Import) continue;

        final String importPrefix = importOrExportInfo.getImportPrefix();
        if (importPrefix == null || !prefix.equals(StringUtil.unquoteString(importPrefix))) continue;

        return getImportedFile(project, virtualFile, importOrExportInfo.getUri());
      }
    }
    return null;
  }

  public static void processTopLevelDeclarations(final @NotNull PsiElement context,
                                                 final @NotNull DartPsiScopeProcessor processor,
                                                 final @NotNull List<VirtualFile> files,
                                                 final @Nullable String componentNameHint) {
    for (VirtualFile virtualFile : files) {
      if (!processTopLevelDeclarations(context, processor, virtualFile, componentNameHint)) {
        break;
      }
    }
  }

  public static boolean processTopLevelDeclarations(final @NotNull PsiElement context,
                                                    final @NotNull DartPsiScopeProcessor processor,
                                                    final @Nullable VirtualFile rootVirtualFile,
                                                    final @Nullable String componentNameHint) {
    final Set<String> fileNames = new THashSet<String>();
    for (VirtualFile virtualFile : DartComponentIndex.getAllFiles(context.getProject(), componentNameHint)) {
      fileNames.add(virtualFile.getName());
    }
    return processTopLevelDeclarationsImpl(context, processor, rootVirtualFile, componentNameHint == null ? null : fileNames,
                                           new THashSet<VirtualFile>(), componentNameHint != null && componentNameHint.startsWith("_"));
  }

  private static boolean processTopLevelDeclarationsImpl(final @NotNull PsiElement context,
                                                         final @NotNull DartPsiScopeProcessor processor,
                                                         final @Nullable VirtualFile virtualFile,
                                                         final @Nullable Set<String> fileNames,
                                                         final @NotNull Set<VirtualFile> processedFiles) {
    return processTopLevelDeclarationsImpl(context, processor, virtualFile, fileNames, processedFiles, false);
  }

  private static boolean processTopLevelDeclarationsImpl(final @NotNull PsiElement context,
                                                         final @NotNull DartPsiScopeProcessor processor,
                                                         final @Nullable VirtualFile virtualFile,
                                                         final @Nullable Set<String> fileNames,
                                                         final @NotNull Set<VirtualFile> processedFiles,
                                                         final boolean isLookingForPrivate) {
    if (virtualFile == null) return false;

    if (processedFiles.contains(virtualFile)) {
      processor.processFilteredOutElementsForImportedFile(virtualFile);
      return true;
    }

    processedFiles.add(virtualFile);

    boolean contains = fileNames == null || fileNames.contains(virtualFile.getName());
    if (contains) {
      final PsiFile psiFile = context.getManager().findFile(virtualFile);
      for (PsiElement root : findDartRoots(psiFile)) {
        if (!DartPsiCompositeElementImpl.processDeclarationsImpl(root, processor, ResolveState.initial(), null)) {
          return false;
        }
      }
    }

    for (String relativePathOrUrl : DartPathIndex.getPaths(context.getProject(), virtualFile)) {
      if (fileNames != null && !fileNames.contains(PathUtil.getFileName(relativePathOrUrl))) {
        continue;
      }
      VirtualFile childFile = findRelativeFile(virtualFile, relativePathOrUrl);
      childFile = childFile != null ? childFile : VirtualFileManager.getInstance().findFileByUrl(relativePathOrUrl);
      if (childFile == null || processedFiles.contains(childFile)) {
        continue;
      }
      final PsiFile childPsiFile = context.getManager().findFile(childFile);
      if (childPsiFile != null) {
        if (!processTopLevelDeclarationsImpl(childPsiFile, processor, childFile, fileNames, processedFiles)) {
          return false;
        }
      }
    }

    if (isLookingForPrivate) {
      return true;
    }

    final List<VirtualFile> libraryFiles = findLibrary(context.getContainingFile());
    final boolean processingLibraryWhereContextElementLocated = libraryFiles.contains(virtualFile);

    for (DartImportOrExportInfo importOrExportInfo : DartImportAndExportIndex.getImportAndExportInfos(context.getProject(), virtualFile)) {
      if (processingLibraryWhereContextElementLocated && importOrExportInfo.getKind() == Kind.Export) continue;
      if (!processingLibraryWhereContextElementLocated && importOrExportInfo.getKind() == Kind.Import) continue;

      // if statement has prefix all components are prefix.Name
      if (importOrExportInfo.getKind() == Kind.Import && importOrExportInfo.getImportPrefix() != null) continue;

      final VirtualFile importedFile = getImportedFile(context.getProject(), virtualFile, importOrExportInfo.getUri());
      if (importedFile != null) {
        processor.importedFileProcessingStarted(importedFile, importOrExportInfo);
        final boolean continueProcessing = processTopLevelDeclarationsImpl(context, processor, importedFile, fileNames, processedFiles);
        processor.importedFileProcessingFinished(importedFile);
        if (!continueProcessing) return false;
      }
    }

    return true;
  }

  @Nullable
  private static VirtualFile getImportedFile(final @NotNull Project project,
                                             final @NotNull VirtualFile contextFile,
                                             final @NotNull String importText) {
    if (importText.startsWith(DART_PREFIX) || importText.startsWith(PACKAGE_PREFIX) || importText.startsWith(FILE_PREFIX)) {
      return DartUrlResolver.getInstance(project, contextFile).findFileByDartUrl(importText);
    }

    final VirtualFile parent = contextFile.getParent();
    return parent == null ? null : VfsUtilCore.findRelativeFile(importText, parent);
  }

  @Nullable
  public static VirtualFile getRealVirtualFile(PsiFile psiFile) {
    return psiFile != null ? psiFile.getOriginalFile().getVirtualFile() : null;
  }

  @Nullable
  public static VirtualFile findRelativeFile(@NotNull VirtualFile file, String path) {
    final VirtualFile parent = file.getParent();
    if (parent == null) {
      return VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path));
    }
    return VfsUtilCore.findRelativeFile(path, parent);
  }

  public static boolean sameLibrary(@NotNull PsiElement context1, @NotNull PsiElement context2) {
    final List<VirtualFile> librariesForContext1 = findLibrary(context1.getContainingFile());
    if (librariesForContext1.isEmpty()) return false;
    final List<VirtualFile> librariesForContext2 = findLibrary(context2.getContainingFile());
    if (librariesForContext2.isEmpty()) return false;
    final THashSet<VirtualFile> librariesSetForContext1 = new THashSet<VirtualFile>(librariesForContext1);
    return ContainerUtil.find(librariesForContext2, new Condition<VirtualFile>() {
      @Override
      public boolean value(VirtualFile file) {
        return librariesSetForContext1.contains(file);
      }
    }) != null;
  }

  @NotNull
  public static List<VirtualFile> findLibrary(final PsiFile context) {
    return findLibrary(context, GlobalSearchScope.allScope(context.getProject()));
  }

  public static List<VirtualFile> findLibrary(final PsiFile context, GlobalSearchScope scope) {
    // todo wouldn't it be better to resolve 'part of XXX' instead?
    final VirtualFile contextVirtualFile = getRealVirtualFile(context);
    if (isLibraryRoot(context)) {
      DartLibraryStatement libraryStatement = null;
      for (PsiElement root : findDartRoots(context)) {
        libraryStatement = PsiTreeUtil.getChildOfType(root, DartLibraryStatement.class);
        if (libraryStatement != null) break;
      }

      if (libraryStatement == null) {
        return contextVirtualFile == null ? Collections.<VirtualFile>emptyList() : Arrays.asList(contextVirtualFile);
      }
      return DartLibraryIndex.findLibraryClass(context, libraryStatement.getLibraryName());
    }

    return ContainerUtil.filter(
      DartSourceIndex.findLibraries(context, context.getName(), scope),
      new Condition<VirtualFile>() {
        @Override
        public boolean value(VirtualFile virtualFile) {
          // check if path point to context
          for (String path : findSourcePathByFileName(context.getProject(), virtualFile, context.getName())) {
            if (virtualFile != null && Comparing.equal(findRelativeFile(virtualFile, path), contextVirtualFile)) {
              return true;
            }
          }
          return false;
        }
      }
    );
  }

  private static List<String> findSourcePathByFileName(Project project, VirtualFile virtualFile, final String fileName) {
    return ContainerUtil.filter(DartPathIndex.getPaths(project, virtualFile), new Condition<String>() {
      @Override
      public boolean value(String path) {
        return path.endsWith(fileName);
      }
    });
  }

  public static boolean isLibraryRoot(PsiFile psiFile) {
    for (PsiElement root : findDartRoots(psiFile)) {
      if (PsiTreeUtil.getChildOfType(root, DartPartOfStatement.class) != null) return false;
    }
    return true;
  }

  // todo this method must look for main function in library parts as well
  @Nullable
  public static DartFunctionDeclarationWithBodyOrNative getMainFunction(final @Nullable PsiFile file) {
    if (!(file instanceof DartFile)) return null;

    final ArrayList<DartComponentName> result = new ArrayList<DartComponentName>();
    DartPsiCompositeElementImpl.processDeclarationsImpl(file, new DartResolveProcessor(result, "main"), ResolveState.initial(), null);

    for (DartComponentName componentName : result) {
      final PsiElement parent = componentName.getParent();
      if (parent instanceof DartFunctionDeclarationWithBodyOrNative) {
        return (DartFunctionDeclarationWithBodyOrNative)parent;
      }
    }
    return null;
  }

  @Nullable
  public static DartReference getLeftReference(@Nullable final PsiElement node) {
    if (node == null) return null;
    for (PsiElement sibling = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(node, true);
         sibling != null;
         sibling = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(sibling, true)) {
      if (".".equals(sibling.getText())) continue;
      PsiElement candidate = sibling;
      if (candidate instanceof DartType) {
        candidate = ((DartType)sibling).getReferenceExpression();
      }
      return candidate instanceof DartReference && candidate != node ? (DartReference)candidate : null;
    }
    DartReference reference = PsiTreeUtil.getParentOfType(node, DartReference.class, false);
    while (reference != null) {
      final PsiElement parent = reference.getParent();
      if (parent instanceof DartCascadeReferenceExpression) {
        DartReference[] references = PsiTreeUtil.getChildrenOfType(parent, DartReference.class);
        DartReference result = references != null && references.length == 2 ? references[0] : null;
        // if not starts with node
        return !PsiTreeUtil.isAncestor(result, node, false) ? result : null;
      }
      else if (parent instanceof DartReference && parent.getFirstChild() == reference) {
        reference = (DartReference)parent;
      }
      else {
        break;
      }
    }
    return null;
  }

  @NotNull
  public static List<DartComponent> findNamedSubComponents(@NotNull DartClass... rootDartClasses) {
    return findNamedSubComponents(true, rootDartClasses);
  }

  @NotNull
  public static List<DartComponent> findNamedSubComponents(boolean unique, @NotNull DartClass... rootDartClasses) {
    final List<DartComponent> unfilteredResult = findSubComponents(new Function<DartClass, List<DartComponent>>() {
      @Override
      public List<DartComponent> fun(DartClass dartClass) {
        final List<DartComponent> result = new ArrayList<DartComponent>();
        for (DartComponent namedComponent : getNamedSubComponents(dartClass)) {
          if (namedComponent.getName() != null) {
            result.add(namedComponent);
          }
        }
        return result;
      }
    }, rootDartClasses);
    if (!unique) {
      return unfilteredResult;
    }
    return new ArrayList<DartComponent>(namedComponentToMap(unfilteredResult).values());
  }

  public static List<DartOperator> findOperators(AbstractDartPsiClass dartPsiClass) {
    return findSubComponents(new Function<DartClass, List<DartOperator>>() {
      @Override
      public List<DartOperator> fun(DartClass dartClass) {
        final DartOperator[] operators = PsiTreeUtil.getChildrenOfType(getBody(dartClass), DartOperator.class);
        return operators == null ? Collections.<DartOperator>emptyList() : Arrays.asList(operators);
      }
    }, dartPsiClass);
  }

  @NotNull
  public static <T> List<T> findSubComponents(final Function<DartClass, List<T>> fun, @NotNull DartClass... rootDartClasses) {
    final List<T> unfilteredResult = new ArrayList<T>();
    processSuperClasses(new PsiElementProcessor<DartClass>() {
      @Override
      public boolean execute(@NotNull DartClass dartClass) {
        unfilteredResult.addAll(fun.fun(dartClass));
        return true;
      }
    }, rootDartClasses);
    return unfilteredResult;
  }

  public static boolean processSuperClasses(PsiElementProcessor<DartClass> processor, @NotNull DartClass... rootDartClasses) {
    final Set<DartClass> processedClasses = new THashSet<DartClass>();
    final LinkedList<DartClass> classes = new LinkedList<DartClass>();
    classes.addAll(Arrays.asList(rootDartClasses));

    while (!classes.isEmpty()) {
      final DartClass dartClass = classes.pollFirst();
      if (dartClass == null || processedClasses.contains(dartClass)) {
        continue;
      }
      if (!processor.execute(dartClass)) {
        return false;
      }

      ContainerUtil.addIfNotNull(classes, dartClass.getSuperClassResolvedOrObjectClass().getDartClass());

      for (DartType type : getImplementsAndMixinsList(dartClass)) {
        ContainerUtil.addIfNotNull(classes, resolveClassByType(type).getDartClass());
      }
      processedClasses.add(dartClass);
    }

    return true;
  }

  public static void collectSupers(@NotNull final List<DartClass> superClasses,
                                   @NotNull final List<DartClass> superInterfaces,
                                   @Nullable DartClass rootDartClass) {
    processSupers(
      new PsiElementProcessor<DartClass>() {
        @Override
        public boolean execute(@NotNull DartClass dartClass) {
          superClasses.add(dartClass);
          return true;
        }
      }, new PsiElementProcessor<DartClass>() {
        @Override
        public boolean execute(@NotNull DartClass dartClass) {
          superInterfaces.add(dartClass);
          return true;
        }
      }, rootDartClass
    );
  }

  public static void processSupers(@Nullable PsiElementProcessor<DartClass> superClassProcessor,
                                   @Nullable PsiElementProcessor<DartClass> superInterfaceProcessor,
                                   @Nullable DartClass rootDartClass) {
    final Set<DartClass> processedClasses = new THashSet<DartClass>();
    DartClass currentClass = rootDartClass;
    while (currentClass != null) {
      processedClasses.add(currentClass);

      // implements
      for (DartType type : currentClass.getImplementsList()) {
        final DartClass result = resolveClassByType(type).getDartClass();
        if (superInterfaceProcessor == null || result == null || processedClasses.contains(result)) {
          continue;
        }
        if (!superInterfaceProcessor.execute(result)) {
          return;
        }
        if (!processSuperClasses(superInterfaceProcessor, result)) {
          return;
        }
      }

      // mixins
      for (DartType type : currentClass.getMixinsList()) {
        final DartClass result = resolveClassByType(type).getDartClass();
        if (superClassProcessor == null || result == null || processedClasses.contains(result)) {
          continue;
        }
        if (!superClassProcessor.execute(result)) {
          return;
        }
      }

      currentClass = currentClass.getSuperClassResolvedOrObjectClass().getDartClass();
      if (currentClass == null || processedClasses.contains(currentClass)) {
        break;
      }
      if (superClassProcessor != null) {
        if (!superClassProcessor.execute(currentClass)) {
          return;
        }
      }
    }
  }

  public static Map<Pair<String, Boolean>, DartComponent> namedComponentToMap(List<DartComponent> unfilteredResult) {
    final Map<Pair<String, Boolean>, DartComponent> result = new HashMap<Pair<String, Boolean>, DartComponent>();
    for (DartComponent dartComponent : unfilteredResult) {
      // need order
      Pair<String, Boolean> key = Pair.create(dartComponent.getName(), dartComponent.isGetter());
      if (result.containsKey(key)) continue;
      result.put(key, dartComponent);
    }
    return result;
  }

  public static List<DartComponentName> getComponentNames(List<? extends DartComponent> fields, boolean filterPrivate) {
    return getComponentNames(
      filterPrivate ?
      ContainerUtil.filter(fields, new Condition<DartComponent>() {
        @Override
        public boolean value(DartComponent component) {
          return component.isPublic();
        }
      }) :
      fields
    );
  }

  public static List<DartComponentName> getComponentNames(List<? extends DartComponent> fields) {
    return ContainerUtil.filter(ContainerUtil.map(fields, new Function<DartComponent, DartComponentName>() {
      @Override
      public DartComponentName fun(DartComponent component) {
        return component.getComponentName();
      }
    }), Condition.NOT_NULL);
  }

  @NotNull
  public static List<DartComponent> getNamedSubComponentsInOrder(DartClass haxeClass) {
    final List<DartComponent> result = getNamedSubComponents(haxeClass);
    Collections.sort(result, new Comparator<DartComponent>() {
      @Override
      public int compare(DartComponent o1, DartComponent o2) {
        return o1.getTextOffset() - o2.getTextOffset();
      }
    });
    return result;
  }

  @NotNull
  public static List<DartComponent> getNamedSubComponents(DartClass dartClass) {
    PsiElement body = getBody(dartClass);

    final List<DartComponent> result = new ArrayList<DartComponent>();
    if (body == null) {
      return result;
    }
    final DartComponent[] namedComponents = PsiTreeUtil.getChildrenOfType(body, DartComponent.class);
    final DartVarDeclarationList[] variables = PsiTreeUtil.getChildrenOfType(body, DartVarDeclarationList.class);
    if (namedComponents != null) {
      ContainerUtil.addAll(result, namedComponents);
    }
    if (variables == null) {
      return result;
    }
    for (DartVarDeclarationList varDeclarationList : variables) {
      result.add(varDeclarationList.getVarAccessDeclaration());
      result.addAll(varDeclarationList.getVarDeclarationListPartList());
    }
    return result;
  }

  @Nullable
  public static DartClassMembers getBody(@Nullable final DartClass dartClass) {
    final DartClassBody body = dartClass == null ? null : ((DartClassDefinition)dartClass).getClassBody();
    return body == null ? null : body.getClassMembers();
  }

  public static List<DartComponent> filterComponentsByType(List<DartComponent> components, final DartComponentType type) {
    return ContainerUtil.filter(components, new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return type == DartComponentType.typeOf(component);
      }
    });
  }

  public static List<DartType> getTypes(@Nullable DartTypeList typeList) {
    if (typeList == null) {
      return Collections.emptyList();
    }
    return typeList.getTypeList();
  }

  public static List<DartClassResolveResult> resolveClassesByTypes(List<DartType> types) {
    return ContainerUtil.map(types, new Function<DartType, DartClassResolveResult>() {
      @Override
      public DartClassResolveResult fun(DartType dartType) {
        return resolveClassByType(dartType);
      }
    });
  }

  @NotNull
  public static DartClassResolveResult resolveClassByType(@Nullable DartType dartType) {
    return resolveClassByType(dartType, DartClassResolveResult.EMPTY);
  }

  private static DartClassResolveResult resolveClassByType(DartType dartType, DartClassResolveResult initializer) {
    if (dartType == null) {
      return DartClassResolveResult.EMPTY;
    }

    final PsiElement target = dartType.resolveReference();
    if (target instanceof DartComponentName) {
      final PsiElement targetParent = target.getParent();
      if (targetParent == initializer.getDartClass()) {
        return initializer;
      }
      if (targetParent instanceof DartClass) {
        return DartClassResolveResult.create((DartClass)targetParent);
      }
      // todo: fix
      // prefix.ClassName or ClassName.name ?
      if (DartComponentType.typeOf(targetParent) == DartComponentType.CONSTRUCTOR) {
        return DartClassResolveResult.create(PsiTreeUtil.getParentOfType(target, DartClass.class));
      }
    }
    return DartClassResolveResult.EMPTY;
  }

  @NotNull
  public static DartClassResolveResult getDartClassResolveResult(@Nullable PsiElement element) {
    return getDartClassResolveResult(element, new DartGenericSpecialization());
  }

  @NotNull
  public static DartClassResolveResult getDartClassResolveResult(@Nullable PsiElement element,
                                                                 @NotNull DartGenericSpecialization specialization) {
    if (element == null) {
      return DartClassResolveResult.create(null);
    }
    final PsiElement parentElement = element.getParent();
    if (element instanceof DartComponentName) {
      return getDartClassResolveResult(parentElement, specialization);
    }
    if (element instanceof DartClass) {
      final DartClass dartClass = (DartClass)element;
      return DartClassResolveResult.create(dartClass, specialization);
    }

    DartClassResolveResult result = tryFindTypeAndResolveClass(element, specialization);
    if (result.getDartClass() != null) {
      return result;
    }

    PsiElement functionBody = PsiTreeUtil.getChildOfType(element, DartFunctionBody.class);
    if (functionBody == null) {
      functionBody = PsiTreeUtil.getChildOfType(element, DartFunctionExpressionBody.class);
    }
    DartReference functionBodyExpression = PsiTreeUtil.getChildOfType(functionBody, DartReference.class);
    if (functionBodyExpression != null) {
      return functionBodyExpression.resolveDartClass();
    }

    if (specialization.containsKey(null, element.getText())) {
      return specialization.get(null, element.getText());
    }

    if (element instanceof DartVarAccessDeclaration && parentElement instanceof DartForInPart) {
      final DartForInPart forInPart = (DartForInPart)parentElement;
      return resolveForInPartClass(forInPart);
    }

    if (element instanceof DartForInPart) {
      final DartForInPart forInPart = (DartForInPart)element;
      return resolveForInPartClass(forInPart);
    }

    if (element instanceof DartSimpleFormalParameter &&
        parentElement instanceof DartNormalFormalParameter &&
        parentElement.getParent() instanceof DartFormalParameterList &&
        parentElement.getParent().getParent() instanceof DartFunctionExpression &&
        parentElement.getParent().getParent().getParent() instanceof DartArgumentList) {
      final int parameterIndex = getParameterIndex(parentElement, ((DartFormalParameterList)parentElement.getParent()));
      final int argumentIndex = getArgumentIndex(parentElement.getParent().getParent());
      final DartCallExpression callExpression = PsiTreeUtil.getParentOfType(element, DartCallExpression.class);
      final DartReference callReference = callExpression == null ? null : (DartReference)callExpression.getExpression();
      final PsiElement target = callReference == null ? null : callReference.resolve();
      final PsiElement argument = target == null ? null : findParameter(target.getParent(), argumentIndex);
      if (argument instanceof DartNormalFormalParameter) {
        final DartType dartType = findParameterType(((DartNormalFormalParameter)argument).getFunctionSignature(), parameterIndex);
        final DartClassResolveResult callClassResolveResult = getLeftClassResolveResult(callReference);
        return getDartClassResolveResult(dartType, callClassResolveResult.getSpecialization());
      }
      return DartClassResolveResult.EMPTY;
    }

    final DartVarInit varInit = PsiTreeUtil.getChildOfType(
      element instanceof DartVarDeclarationListPart ? element : parentElement,
      DartVarInit.class
    );
    final DartExpression initExpression = varInit == null ? null : varInit.getExpression();
    if (initExpression instanceof DartReference) {
      result = ((DartReference)initExpression).resolveDartClass();
      result.specialize(initExpression);
      return result;
    }
    return getDartClassResolveResult(initExpression);
  }

  private static DartClassResolveResult getLeftClassResolveResult(DartReference reference) {
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(reference, DartReference.class);
    if (references != null && references.length == 2) {
      return references[0].resolveDartClass();
    }
    return DartClassResolveResult.create(PsiTreeUtil.getChildOfType(reference, DartClass.class));
  }

  @Nullable
  public static DartComponentName findParameterByName(@Nullable DartFormalParameterList parameterList, final String name) {
    if (parameterList == null) {
      return null;
    }

    for (DartNormalFormalParameter parameter : parameterList.getNormalFormalParameterList()) {
      final DartComponentName componentName = parameter.findComponentName();
      if (componentName != null && name.equals(componentName.getText())) return componentName;
    }

    final DartNamedFormalParameters namedFormalParameters = parameterList.getNamedFormalParameters();
    if (namedFormalParameters == null) return null;

    for (DartDefaultFormalNamedParameter namedParameter : namedFormalParameters.getDefaultFormalNamedParameterList()) {
      final DartComponentName parameterName = namedParameter.getNormalFormalParameter().findComponentName();
      if (parameterName != null && name.equals(parameterName.getText())) return parameterName;
    }
    return null;
  }

  @Nullable
  private static PsiElement findParameter(@Nullable PsiElement element, int index) {
    final DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
    if (parameterList == null) {
      return null;
    }
    final int normalParameterSize = parameterList.getNormalFormalParameterList().size();
    if (index < normalParameterSize) {
      return parameterList.getNormalFormalParameterList().get(index);
    }
    final DartNamedFormalParameters namedFormalParameters = parameterList.getNamedFormalParameters();
    return namedFormalParameters == null
           ? null
           : namedFormalParameters.getDefaultFormalNamedParameterList().get(index - normalParameterSize);
  }

  @Nullable
  private static DartType findParameterType(PsiElement element, int index) {
    final PsiElement target = findParameter(element, index);
    return findType(target);
  }

  private static int getParameterIndex(@NotNull PsiElement element, @NotNull DartFormalParameterList parameterList) {
    int normalIndex = parameterList.getNormalFormalParameterList().indexOf(element);
    final DartNamedFormalParameters formalParameters = parameterList.getNamedFormalParameters();
    int namedIndex = formalParameters == null ? -1 : formalParameters.getDefaultFormalNamedParameterList().indexOf(element);
    return normalIndex >= 0 ? normalIndex :
           namedIndex >= 0 ? namedIndex + parameterList.getNormalFormalParameterList().size() : -1;
  }

  private static DartClassResolveResult resolveForInPartClass(DartForInPart forInPart) {
    final DartExpression expression = forInPart.getExpression();
    final DartReference dartReference = expression instanceof DartReference ? (DartReference)expression : null;
    final DartClassResolveResult classResolveResult =
      dartReference == null ? DartClassResolveResult.EMPTY : dartReference.resolveDartClass();
    final DartClass dartClass = classResolveResult.getDartClass();
    final DartClassResolveResult iteratorResult = dartClass == null ? DartClassResolveResult.EMPTY :
                                                  getDartClassResolveResult(dartClass.findMemberByName("iterator"),
                                                                            classResolveResult.getSpecialization());
    final DartClassResolveResult finalResult = iteratorResult.getSpecialization().get(null, "E");
    return finalResult == null ? DartClassResolveResult.EMPTY : finalResult;
  }

  public static int getArgumentIndex(PsiElement place) {
    int parameterIndex = -1;
    final DartArgumentList argumentList = PsiTreeUtil.getParentOfType(place, DartArgumentList.class, false);
    if (place == argumentList) {
      assert argumentList != null;
      final DartFunctionDescription functionDescription = DartFunctionDescription.tryGetDescription(
        (DartCallExpression)argumentList.getParent());
      // the last one
      parameterIndex = functionDescription == null ? -1 : functionDescription.getParameters().length - 1;
    }
    else if (argumentList != null) {
      for (DartExpression expression : argumentList.getExpressionList()) {
        ++parameterIndex;
        if (expression.getTextRange().getEndOffset() >= place.getTextOffset()) {
          break;
        }
      }
    }
    else if (UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(place, true) instanceof DartArgumentList) {
      // seems foo(param1, param2<caret>)
      final DartArgumentList prevSibling = (DartArgumentList)UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(place, true);
      assert prevSibling != null;
      // callExpression -> arguments -> argumentList
      parameterIndex = prevSibling.getExpressionList().size() + prevSibling.getNamedArgumentList().size();
    }
    else if (PsiTreeUtil.getParentOfType(place, DartCallExpression.class, true) != null) {
      // seems foo(<caret>)
      parameterIndex = 0;
    }
    return parameterIndex;
  }

  @NotNull
  private static DartClassResolveResult tryFindTypeAndResolveClass(@Nullable PsiElement element,
                                                                   DartGenericSpecialization specialization) {
    DartType type = PsiTreeUtil.getChildOfType(element, DartType.class);
    if (type == null && element instanceof DartType) {
      type = (DartType)element;
    }
    else if (type == null) {
      final DartReturnType returnType = PsiTreeUtil.getChildOfType(element, DartReturnType.class);
      type = returnType == null ? null : returnType.getType();
    }

    if (type == null && element instanceof DartVarDeclarationListPart) {
      final PsiElement parent = element.getParent();
      if (parent instanceof DartVarDeclarationList) {
        type = ((DartVarDeclarationList)parent).getVarAccessDeclaration().getType();
      }
    }
    DartClass dartClass = type == null ? null : resolveClassByType(type).getDartClass();

    if (dartClass == null && type != null && specialization.containsKey(element, type.getText())) {
      return specialization.get(element, type.getText());
    }

    DartClassResolveResult result = getDartClassResolveResult(dartClass, specialization.getInnerSpecialization(element));
    if (result.getDartClass() != null) {
      result.specializeByParameters(type == null ? null : type.getTypeArguments());
      return result;
    }

    return DartClassResolveResult.EMPTY;
  }

  @NotNull
  public static String getOperatorString(@Nullable PsiElement element) {
    if (element == null) {
      return "";
    }
    final StringBuilder result = new StringBuilder();
    element.accept(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        if (element instanceof LeafPsiElement && DartTokenTypesSets.OPERATORS.contains(((LeafPsiElement)element).getElementType())) {
          result.append(element.getText());
        }
        super.visitElement(element);
      }
    });
    return result.toString();
  }

  @Nullable
  public static DartClass suggestType(PsiElement element) {
    DartAssignExpression assignExpression = PsiTreeUtil.getParentOfType(element, DartAssignExpression.class);
    if (assignExpression != null) {
      DartReference[] references = PsiTreeUtil.getChildrenOfType(assignExpression, DartReference.class);
      if (references != null && references.length > 1) {
        return references[1].resolveDartClass().getDartClass();
      }
    }
    return null;
  }

  @Nullable
  public static DartComponent findReferenceAndComponentTarget(@Nullable PsiElement element) {
    DartReference reference = PsiTreeUtil.getParentOfType(element, DartReference.class);
    PsiElement target = reference == null ? null : reference.resolve();
    PsiElement targetParent = target != null ? target.getParent() : null;
    if (targetParent instanceof DartComponent) {
      return (DartComponent)targetParent;
    }
    return null;
  }

  public static boolean aloneOrFirstInChain(DartReference reference) {
    return PsiTreeUtil.getChildrenOfType(reference, DartReference.class) == null
           && getLeftReference(reference) == null
           && getLeftReference(reference.getParent()) == null;
  }

  @NotNull
  public static ResolveResult[] toCandidateInfoArray(@Nullable List<? extends PsiElement> elements) {
    if (elements == null) {
      return ResolveResult.EMPTY_ARRAY;
    }
    elements = ContainerUtil.filter(elements, new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement element) {
        return element != null;
      }
    });
    final ResolveResult[] result = new ResolveResult[elements.size()];
    for (int i = 0, size = elements.size(); i < size; i++) {
      result[i] = new PsiElementResolveResult(elements.get(i));
    }
    return result;
  }

  public static void treeWalkUpAndTopLevelDeclarations(final @NotNull PsiElement context, final @NotNull DartPsiScopeProcessor processor) {
    PsiTreeUtil.treeWalkUp(processor, context, null, ResolveState.initial());

    final List<VirtualFile> libraryFiles = new ArrayList<VirtualFile>();
    libraryFiles.addAll(findLibrary(context.getContainingFile()));
    libraryFiles.addAll(DartLibraryIndex.findLibraryClass(context, "dart:core"));

    processTopLevelDeclarations(context, processor, libraryFiles, null);
  }

  public static List<DartType> getImplementsAndMixinsList(DartClass dartClass) {
    return ContainerUtil.concat(dartClass.getImplementsList(), dartClass.getMixinsList());
  }

  public static List<DartComponent> findNamedSuperComponents(@Nullable DartClass dartClass) {
    if (dartClass == null) {
      return ContainerUtilRt.emptyList();
    }
    final List<DartClass> supers = new ArrayList<DartClass>();
    final DartClassResolveResult dartClassResolveResult = dartClass.getSuperClassResolvedOrObjectClass();
    if (dartClassResolveResult.getDartClass() != null) {
      supers.add(dartClassResolveResult.getDartClass());
    }
    List<DartClassResolveResult> implementsAndMixinsList = resolveClassesByTypes(
      getImplementsAndMixinsList(dartClass)
    );
    for (DartClassResolveResult resolveResult : implementsAndMixinsList) {
      final DartClass resolveResultDartClass = resolveResult.getDartClass();
      if (resolveResultDartClass != null) {
        supers.add(resolveResultDartClass);
      }
    }
    return findNamedSubComponents(supers.toArray(new DartClass[supers.size()]));
  }
}
