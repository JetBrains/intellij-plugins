// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.BooleanValueHolder;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.ide.index.*;
import com.jetbrains.lang.dart.ide.info.DartFunctionDescription;
import com.jetbrains.lang.dart.ide.info.DartOptionalParameterDescription;
import com.jetbrains.lang.dart.ide.info.DartParameterDescription;
import com.jetbrains.lang.dart.ide.info.DartParameterInfoHandler;
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
import static com.jetbrains.lang.dart.util.DartUrlResolver.DART_CORE_URI;

public class DartResolveUtil {

  public static final String OBJECT = "Object";

  public static List<PsiElement> findDartRoots(@Nullable final PsiFile psiFile) {
    if (psiFile instanceof XmlFile) {
      return findDartRootsInXml((XmlFile)psiFile);
    }
    return psiFile instanceof DartFile ? Collections.singletonList(psiFile) : Collections.emptyList();
  }

  private static List<PsiElement> findDartRootsInXml(XmlFile xmlFile) {
    final List<PsiElement> result = new ArrayList<>();
    xmlFile.acceptChildren(new XmlRecursiveElementWalkingVisitor() {
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
    final DartOptionalFormalParameters optionalFormalParameters = list.getOptionalFormalParameters();
    if (optionalFormalParameters == null) {
      return true;
    }
    for (int size = optionalFormalParameters.getDefaultFormalNamedParameterList().size();
         i - normalFormalParameterList.size() < size;
         ++i) {
      final DartDefaultFormalNamedParameter defaultFormalParameter =
        optionalFormalParameters.getDefaultFormalNamedParameterList().get(i - normalFormalParameterList.size());
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
    processSuperClasses(dartClass -> {
      if (dartClass == baseClass) {
        result.setValue(true);
        return false;
      }
      return true;
    }, aClass);
    return result.getValue();
  }

  @Nullable
  public static DartType findType(@Nullable PsiElement element) {
    if (element instanceof DartDefaultFormalNamedParameter) {
      return findType(((DartDefaultFormalNamedParameter)element).getNormalFormalParameter());
    }
    if (element instanceof DartNormalFormalParameter) {
      //final DartFunctionFormalParameter functionFormalParameter = ((DartNormalFormalParameter)element).getFunctionFormalParameter();
      final DartFieldFormalParameter fieldFormalParameter = ((DartNormalFormalParameter)element).getFieldFormalParameter();
      final DartSimpleFormalParameter simpleFormalParameter = ((DartNormalFormalParameter)element).getSimpleFormalParameter();

      // todo return some FUNCTION type?
      //if (functionFormalParameter != null) {}

      if (fieldFormalParameter != null) return fieldFormalParameter.getType();
      if (simpleFormalParameter != null) return simpleFormalParameter.getType();
    }
    return null;
  }

  @NotNull
  public static DartClassResolveResult findCoreClass(PsiElement context, String className) {
    final VirtualFile dartCoreLib = DartLibraryIndex.getSdkLibByUri(context.getProject(), DART_CORE_URI);
    final List<DartComponentName> result = new ArrayList<>();
    processTopLevelDeclarations(context, new DartResolveProcessor(result, className), dartCoreLib, className);
    final PsiElement parent = result.isEmpty() ? null : result.iterator().next().getParent();
    return DartClassResolveResult.create(parent instanceof DartClass ? (DartClass)parent : null);
  }

  @NotNull
  public static String getLibraryName(@NotNull final PsiFile psiFile) {
    for (PsiElement root : findDartRoots(psiFile)) {
      final DartLibraryStatement libraryStatement = PsiTreeUtil.getChildOfType(root, DartLibraryStatement.class);
      if (libraryStatement != null) {
        return libraryStatement.getLibraryNameElement().getName();
      }

      final DartPartOfStatement partOfStatement = PsiTreeUtil.getChildOfType(root, DartPartOfStatement.class);
      if (partOfStatement != null) {
        return partOfStatement.getLibraryName();
      }
    }

    return psiFile.getName();
  }

  @NotNull
  public static Collection<DartClass> getClassDeclarations(@NotNull final PsiElement root) {
    final List<DartClass> result = new SmartList<>();
    for (PsiElement child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof DartClass) {
        result.add((DartClass)child);
      }
    }

    return result;
  }

  public static void processTopLevelDeclarations(final @NotNull PsiElement context,
                                                 final @NotNull DartPsiScopeProcessor processor,
                                                 final @NotNull List<? extends VirtualFile> files,
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
    final Set<VirtualFile> filesOfInterest =
      componentNameHint == null ? null : (Set<VirtualFile>)DartComponentIndex.getAllFiles(componentNameHint, context.getResolveScope());

    if (filesOfInterest != null && filesOfInterest.isEmpty()) return true;

    final boolean privateOnly = componentNameHint != null && componentNameHint.startsWith("_");
    return processTopLevelDeclarationsImpl(context, processor, rootVirtualFile, filesOfInterest, new THashSet<>(), privateOnly);
  }

  private static boolean processTopLevelDeclarationsImpl(final @NotNull PsiElement context,
                                                         final @NotNull DartPsiScopeProcessor processor,
                                                         final @Nullable VirtualFile virtualFile,
                                                         final @Nullable Set<? extends VirtualFile> filesOfInterest,
                                                         final @NotNull Set<? super VirtualFile> alreadyProcessed,
                                                         final boolean privateOnly) {
    if (virtualFile == null) return true;

    if (alreadyProcessed.contains(virtualFile)) {
      processor.processFilteredOutElementsForImportedFile(virtualFile);
      return true;
    }

    alreadyProcessed.add(virtualFile);

    boolean contains = filesOfInterest == null || filesOfInterest.contains(virtualFile);
    if (contains) {
      final PsiFile psiFile = context.getManager().findFile(virtualFile);
      for (PsiElement root : findDartRoots(psiFile)) {
        if (!DartPsiCompositeElementImpl.processDeclarationsImpl(root, processor, ResolveState.initial(), null)) {
          return false;
        }
      }
    }

    for (String partUrl : DartPartUriIndex.getPartUris(context.getProject(), virtualFile)) {
      final VirtualFile partFile = getImportedFile(context.getProject(), virtualFile, partUrl);
      if (partFile == null || alreadyProcessed.contains(partFile) || (filesOfInterest != null && !filesOfInterest.contains(partFile))) {
        continue;
      }

      final PsiFile partPsiFile = context.getManager().findFile(partFile);
      if (partPsiFile != null) {
        if (!processTopLevelDeclarationsImpl(partPsiFile, processor, partFile, filesOfInterest, alreadyProcessed, privateOnly)) {
          return false;
        }
      }
    }

    if (privateOnly) {
      return true;
    }

    final List<VirtualFile> libraryFiles = findLibrary(context.getContainingFile());
    final boolean processingLibraryWhereContextElementLocated = libraryFiles.contains(virtualFile);

    boolean coreImportedExplicitly = false;

    for (DartImportOrExportInfo importOrExportInfo : DartImportAndExportIndex.getImportAndExportInfos(context.getProject(), virtualFile)) {
      if (processingLibraryWhereContextElementLocated && importOrExportInfo.getKind() == Kind.Export) continue;
      if (!processingLibraryWhereContextElementLocated && importOrExportInfo.getKind() == Kind.Import) continue;

      if (importOrExportInfo.getKind() == Kind.Import && DART_CORE_URI.equals(importOrExportInfo.getUri())) {
        coreImportedExplicitly = true;
      }

      // if statement has prefix all components are prefix.Name
      if (importOrExportInfo.getKind() == Kind.Import && importOrExportInfo.getImportPrefix() != null) continue;

      final VirtualFile importedFile = getImportedFile(context.getProject(), virtualFile, importOrExportInfo.getUri());
      if (importedFile != null) {
        processor.importedFileProcessingStarted(importedFile, importOrExportInfo);
        final boolean continueProcessing =
          processTopLevelDeclarationsImpl(context, processor, importedFile, filesOfInterest, alreadyProcessed, false);
        processor.importedFileProcessingFinished(importedFile);
        if (!continueProcessing) {
          return false;
        }
      }
    }

    if (!coreImportedExplicitly && processingLibraryWhereContextElementLocated) {
      final VirtualFile dartCoreLib = DartLibraryIndex.getSdkLibByUri(context.getProject(), DART_CORE_URI);
      if (dartCoreLib != null) {
        final DartImportOrExportInfo implicitImportInfo =
          new DartImportOrExportInfo(Kind.Import, DART_CORE_URI, null, Collections.emptySet(), Collections.emptySet());
        processor.importedFileProcessingStarted(dartCoreLib, implicitImportInfo);
        final boolean continueProcessing =
          processTopLevelDeclarationsImpl(context, processor, dartCoreLib, filesOfInterest, alreadyProcessed, false);
        processor.importedFileProcessingFinished(dartCoreLib);

        if (!continueProcessing) {
          return false;
        }
      }
    }

    return true;
  }

  @Nullable
  public static VirtualFile getImportedFile(final @NotNull Project project,
                                            final @NotNull VirtualFile contextFile,
                                            final @NotNull String importText) {
    if (importText.startsWith(DartUrlResolver.DART_PREFIX) ||
        importText.startsWith(DartUrlResolver.PACKAGE_PREFIX) ||
        importText.startsWith(DartUrlResolver.FILE_PREFIX)) {
      return DartUrlResolver.getInstance(project, contextFile).findFileByDartUrl(importText);
    }

    final VirtualFile parent = contextFile.getParent();
    return parent == null ? null : VfsUtilCore.findRelativeFile(importText, parent);
  }

  @Nullable
  public static VirtualFile getRealVirtualFile(PsiFile psiFile) {
    return psiFile != null ? psiFile.getOriginalFile().getVirtualFile() : null;
  }

  public static boolean sameLibrary(@NotNull PsiElement context1, @NotNull PsiElement context2) {
    final List<VirtualFile> librariesForContext1 = findLibrary(context1.getContainingFile());
    if (librariesForContext1.isEmpty()) return false;
    final List<VirtualFile> librariesForContext2 = findLibrary(context2.getContainingFile());
    if (librariesForContext2.isEmpty()) return false;
    final THashSet<VirtualFile> librariesSetForContext1 = new THashSet<>(librariesForContext1);
    return ContainerUtil.find(librariesForContext2, librariesSetForContext1::contains) != null;
  }

  @NotNull
  public static List<VirtualFile> findLibrary(@NotNull final PsiFile context) {
    final VirtualFile contextVirtualFile = getRealVirtualFile(context);
    if (contextVirtualFile == null) return Collections.emptyList();

    return CachedValuesManager.getCachedValue(context, () -> {
      for (PsiElement root : findDartRoots(context)) {
        final DartPartOfStatement partOfStatement = PsiTreeUtil.getChildOfType(root, DartPartOfStatement.class);
        if (partOfStatement != null) {
          final String libraryName = partOfStatement.getLibraryName();
          final List<VirtualFile> files = findLibraryByName(context, libraryName);
          if (!files.isEmpty()) {
            return new CachedValueProvider.Result<>(files, PsiModificationTracker.MODIFICATION_COUNT);
          }
        }
      }

      // no 'part of' statement in file -> this file itself is a library
      return new CachedValueProvider.Result<>(Collections.singletonList(contextVirtualFile), PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  @NotNull
  public static List<VirtualFile> findLibraryByName(@NotNull final PsiElement context, @NotNull final String libraryName) {
    return ContainerUtil.filter(DartLibraryIndex.getFilesByLibName(context.getResolveScope(), libraryName), mainLibFile -> {
      for (String partUrl : DartPartUriIndex.getPartUris(context.getProject(), mainLibFile)) {
        final VirtualFile partFile = getImportedFile(context.getProject(), mainLibFile, partUrl);
        if (Comparing.equal(getRealVirtualFile(context.getContainingFile()), partFile)) return true;
      }

      return false;
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

    final ArrayList<DartComponentName> result = new ArrayList<>();
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
      String siblingText = sibling.getText();
      // String.equals() is fast so use it instead of trying to optimize this.
      if (".".equals(siblingText)) continue;
      if ("..".equals(siblingText)) continue;
      if ("?.".equals(siblingText)) continue;
      PsiElement candidate = sibling;
      if (candidate instanceof DartType) {
        candidate = ((DartType)sibling).getReferenceExpression();
      }
      return candidate instanceof DartReference && candidate != node ? (DartReference)candidate : null;
    }
    DartReference reference = PsiTreeUtil.getParentOfType(node, DartReference.class, false);
    while (reference != null) {
      PsiElement parent = reference.getParent();
      if (parent instanceof DartCascadeReferenceExpression) {
        parent = parent.getParent();
        if (parent instanceof DartValueExpression) {
          final List<DartExpression> expressionList = ((DartValueExpression)parent).getExpressionList();
          final DartExpression firstExpression = expressionList.isEmpty() ? null : expressionList.get(0);
          if (firstExpression instanceof DartReference) {
            return (DartReference)firstExpression;
          }
        }
        // Invalid tree shape
        return null;
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
    final List<DartComponent> unfilteredResult = findSubComponents(dartClass -> {
      final List<DartComponent> result = new ArrayList<>();
      for (DartComponent namedComponent : getNamedSubComponents(dartClass)) {
        if (namedComponent.getName() != null) {
          result.add(namedComponent);
        }
      }
      return result;
    }, rootDartClasses);
    if (!unique) {
      return unfilteredResult;
    }
    return new ArrayList<>(namedComponentToMap(unfilteredResult).values());
  }

  public static List<DartMethodDeclaration> findOperators(AbstractDartPsiClass dartPsiClass) {
    return findSubComponents(dartClass -> {
      List<DartMethodDeclaration> operators = Lists.newArrayList();
      final DartMethodDeclaration[] methods = PsiTreeUtil.getChildrenOfType(getBody(dartClass), DartMethodDeclaration.class);
      if (methods != null) {
        for (DartMethodDeclaration method : methods) {
          if (method.isOperator()) {
            operators.add(method);
          }
        }
      }
      return operators;
    }, dartPsiClass);
  }

  @NotNull
  public static <T> List<T> findSubComponents(final Function<? super DartClass, ? extends List<T>> fun,
                                              @NotNull DartClass... rootDartClasses) {
    final List<T> unfilteredResult = new ArrayList<>();
    processSuperClasses(dartClass -> {
      unfilteredResult.addAll(fun.fun(dartClass));
      return true;
    }, rootDartClasses);
    return unfilteredResult;
  }

  public static boolean processSuperClasses(PsiElementProcessor<? super DartClass> processor, @NotNull DartClass... rootDartClasses) {
    final Set<DartClass> processedClasses = new THashSet<>();
    final LinkedList<DartClass> classes = new LinkedList<>();
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

  public static void collectSupers(@NotNull final List<? super DartClass> superClasses,
                                   @NotNull final List<? super DartClass> superInterfaces,
                                   @Nullable DartClass rootDartClass) {
    processSupers(dartClass -> {
      superClasses.add(dartClass);
      return true;
    }, dartClass -> {
      superInterfaces.add(dartClass);
      return true;
    }, rootDartClass);
  }

  public static void processSupers(@Nullable PsiElementProcessor<? super DartClass> superClassProcessor,
                                   @Nullable PsiElementProcessor<? super DartClass> superInterfaceProcessor,
                                   @Nullable DartClass rootDartClass) {
    final Set<DartClass> processedClasses = new THashSet<>();
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

  public static Map<Pair<String, Boolean>, DartComponent> namedComponentToMap(List<? extends DartComponent> unfilteredResult) {
    final Map<Pair<String, Boolean>, DartComponent> result = new HashMap<>();
    for (DartComponent dartComponent : unfilteredResult) {
      // need order
      Pair<String, Boolean> key = Pair.create(dartComponent.getName(), dartComponent.isGetter());
      if (result.containsKey(key)) continue;
      result.put(key, dartComponent);
    }
    return result;
  }

  public static List<DartComponentName> getComponentNames(Collection<? extends DartComponent> fields) {
    return ContainerUtil
      .filter(ContainerUtil.map(fields, (Function<DartComponent, DartComponentName>)DartComponent::getComponentName),
              Condition.NOT_NULL);
  }

  public static DartComponentName[] getComponentNameArray(Collection<? extends DartComponent> components) {
    final List<DartComponentName> names = getComponentNames(components);
    return names.toArray(new DartComponentName[0]);
  }

  @NotNull
  public static List<DartComponent> getNamedSubComponents(DartClass dartClass) {
    if (dartClass.isEnum()) {
      final List<DartEnumConstantDeclaration> enumConstants = dartClass.getEnumConstantDeclarationList();
      final List<DartComponent> result = new ArrayList<>(enumConstants.size());
      result.addAll(enumConstants);
      return result;
    }

    PsiElement body = getBody(dartClass);

    final List<DartComponent> result = new ArrayList<>();
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
    final DartClassBody body;
    if (dartClass instanceof DartClassDefinition) {
      body = ((DartClassDefinition)dartClass).getClassBody();
    }
    else if (dartClass instanceof DartMixinDeclaration) {
      body = ((DartMixinDeclaration)dartClass).getClassBody();
    }
    else {
      body = null;
    }
    return body == null ? null : body.getClassMembers();
  }

  public static List<DartComponent> filterComponentsByType(List<? extends DartComponent> components, final DartComponentType type) {
    return ContainerUtil.filter(components, component -> type == DartComponentType.typeOf(component));
  }

  public static List<DartType> getTypes(@Nullable DartTypeList typeList) {
    if (typeList == null) {
      return Collections.emptyList();
    }
    return typeList.getTypeList();
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

    if (parentElement instanceof DartEnumConstantDeclaration) {
      return getDartClassResolveResult(parentElement.getParent());
    }

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
      final int argumentIndex = getArgumentIndex(parentElement.getParent().getParent(), null);
      final DartCallExpression callExpression = PsiTreeUtil.getParentOfType(element, DartCallExpression.class);
      final DartReference callReference = callExpression == null ? null : (DartReference)callExpression.getExpression();
      final PsiElement target = callReference == null ? null : callReference.resolve();
      final PsiElement argument = target == null ? null : findParameter(target.getParent(), argumentIndex);
      if (argument instanceof DartNormalFormalParameter) {
        final DartType dartType = findParameterType(((DartNormalFormalParameter)argument).getFunctionFormalParameter(), parameterIndex);
        final DartClassResolveResult callClassResolveResult = getLeftClassResolveResult(callReference);
        return getDartClassResolveResult(dartType, callClassResolveResult.getSpecialization());
      }
      return DartClassResolveResult.EMPTY;
    }

    final DartVarInit varInit =
      PsiTreeUtil.getChildOfType(element instanceof DartVarDeclarationListPart ? element : parentElement, DartVarInit.class);
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

  /**
   * Returns the constructor invoked by the given {@link DartNewExpression}.
   * <p/>
   * TODO(scheglov) Add non-named constructor declarations (they're methods now).
   */
  @Nullable
  public static DartComponent findConstructorDeclaration(DartNewExpression newExpression) {
    DartType type = newExpression.getType();
    PsiElement psiElement = type != null ? type.getReferenceExpression() : null;
    PsiElement target = psiElement != null ? ((DartReference)psiElement).resolve() : null;
    return target != null ? (DartComponent)target.getParent() : null;
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
    final DartOptionalFormalParameters optionalFormalParameters = parameterList.getOptionalFormalParameters();
    return optionalFormalParameters == null
           ? null
           : optionalFormalParameters.getDefaultFormalNamedParameterList().get(index - normalParameterSize);
  }

  @Nullable
  private static DartType findParameterType(@Nullable PsiElement element, int index) {
    final PsiElement target = findParameter(element, index);
    return findType(target);
  }

  private static int getParameterIndex(@NotNull PsiElement element, @NotNull DartFormalParameterList parameterList) {
    int normalIndex = parameterList.getNormalFormalParameterList().indexOf(element);
    final DartOptionalFormalParameters formalParameters = parameterList.getOptionalFormalParameters();
    int namedIndex = formalParameters == null ? -1 : formalParameters.getDefaultFormalNamedParameterList().indexOf(element);
    return normalIndex >= 0 ? normalIndex : namedIndex >= 0 ? namedIndex + parameterList.getNormalFormalParameterList().size() : -1;
  }

  private static DartClassResolveResult resolveForInPartClass(DartForInPart forInPart) {
    final DartExpression expression = forInPart.getExpression();
    final DartReference dartReference = expression instanceof DartReference ? (DartReference)expression : null;
    final DartClassResolveResult classResolveResult =
      dartReference == null ? DartClassResolveResult.EMPTY : dartReference.resolveDartClass();
    final DartClass dartClass = classResolveResult.getDartClass();
    final DartClassResolveResult iteratorResult = dartClass == null
                                                  ? DartClassResolveResult.EMPTY
                                                  : getDartClassResolveResult(dartClass.findMemberByName("iterator"),
                                                                              classResolveResult.getSpecialization());
    final DartClassResolveResult finalResult = iteratorResult.getSpecialization().get(null, "E");
    return finalResult == null ? DartClassResolveResult.EMPTY : finalResult;
  }

  public static int getArgumentIndex(PsiElement place, @Nullable DartFunctionDescription functionDescription) {
    int parameterIndex = -1;
    final DartArgumentList argumentList = PsiTreeUtil.getParentOfType(place, DartArgumentList.class, false);
    String selectedArgumentName = null;
    if (place == argumentList) {
      assert argumentList != null;
      final DartFunctionDescription functionDescription2 =
        DartFunctionDescription.tryGetDescription((DartCallExpression)argumentList.getParent());
      // the last one
      parameterIndex = functionDescription2 == null ? -1 : functionDescription2.getParameters().length - 1;
    }
    else if (argumentList != null) {
      final SmartList<DartPsiCompositeElement> allArguments = new SmartList<>();
      allArguments.addAll(argumentList.getExpressionList());
      allArguments.addAll(argumentList.getNamedArgumentList());
      for (DartPsiCompositeElement expression : allArguments) {
        ++parameterIndex;
        if (expression.getTextRange().getEndOffset() >= place.getTextRange().getStartOffset()) {
          if (expression instanceof DartNamedArgument) {
            selectedArgumentName = ((DartNamedArgument)expression).getParameterReferenceExpression().getText();
          }
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
      // If the last argument list doesn't end with a comma, then the index needs to be decremented
      if (prevSibling.getLastChild().getNode().getElementType() != DartTokenTypes.COMMA) {
        parameterIndex--;
      }
    }
    else if (DartParameterInfoHandler.findElementForParameterInfo(place) != null) {
      // foo(<caret>), new Foo(<caret>) or @Foo(<caret>)
      parameterIndex = 0;
    }

    if (functionDescription != null && selectedArgumentName != null) {
      final DartParameterDescription[] dartParameterDescriptions = functionDescription.getParameters();
      final DartOptionalParameterDescription[] dartOptionalParameterDescriptions = functionDescription.getOptionalParameters();

      for (int i = 0; i < dartOptionalParameterDescriptions.length; i++) {
        if (dartOptionalParameterDescriptions[i].getText().contains(selectedArgumentName)) {
          return dartParameterDescriptions.length + i;
        }
      }
    }

    return parameterIndex;
  }

  @NotNull
  private static DartClassResolveResult tryFindTypeAndResolveClass(@Nullable PsiElement element, DartGenericSpecialization specialization) {
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
  public static DartComponent findReferenceAndComponentTarget(@Nullable PsiElement element) {
    DartReference reference = PsiTreeUtil.getNonStrictParentOfType(element, DartReference.class);
    PsiElement target = reference == null ? null : reference.resolve();
    PsiElement targetParent = target != null ? target.getParent() : null;
    if (targetParent instanceof DartComponent) {
      return (DartComponent)targetParent;
    }
    return null;
  }

  public static boolean aloneOrFirstInChain(DartReference reference) {
    return PsiTreeUtil.getChildrenOfType(reference, DartReference.class) == null &&
           getLeftReference(reference) == null &&
           getLeftReference(reference.getParent()) == null;
  }

  @NotNull
  public static ResolveResult[] toCandidateInfoArray(@Nullable List<? extends PsiElement> elements) {
    if (elements == null) {
      return ResolveResult.EMPTY_ARRAY;
    }
    elements = ContainerUtil.filter(elements, (Condition<PsiElement>)Objects::nonNull);
    final ResolveResult[] result = new ResolveResult[elements.size()];
    for (int i = 0, size = elements.size(); i < size; i++) {
      result[i] = new PsiElementResolveResult(elements.get(i));
    }
    return result;
  }

  public static List<DartType> getImplementsAndMixinsList(DartClass dartClass) {
    return ContainerUtil.concat(dartClass.getImplementsList(), dartClass.getMixinsList());
  }
}
