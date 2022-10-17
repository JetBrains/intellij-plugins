package com.intellij.plugins.drools.lang.psi.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.impl.DroolsFakePsiMethod;
import com.intellij.plugins.drools.lang.psi.impl.DroolsPsiClassImpl;
import com.intellij.plugins.drools.lang.psi.util.processors.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors.CollectProcessor;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DroolsResolveUtil {
  public static DroolsDeclarationsProcessor[] myDeclarationsProcessors = new DroolsDeclarationsProcessor[]{
    DroolsImportedPackagesProcessor.getInstance(),
    DroolsImportedClassesProcessor.getInstance(),
    DroolsLhsBindVariablesProcessor.getInstance(),
    DroolsImplicitVariablesProcessor.getInstance(),
    DroolsGlobalVariablesProcessor.getInstance(),
    DroolsFunctionsProcessor.getInstance(),
    DroolsDeclaredTypesProcessor.getInstance(),
    DroolsLocalVariablesProcessor.getInstance(),
    DroolsRhsImplicitAssignExpressionsProcessor.getInstance()
  };

  public static boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                            @NotNull ResolveState state,
                                            PsiElement lastParent,
                                            @NotNull PsiElement place) {

    DroolsFile droolsFile = PsiTreeUtil.getContextOfType(place, DroolsFile.class);
    if (droolsFile != null) {
      for (DroolsDeclarationsProcessor declarationsProcessor : myDeclarationsProcessors) {
        if (!declarationsProcessor.processElement(processor, state, lastParent, place, droolsFile)) return false;
      }
    }
    return true;
  }

  public static Collection<? extends PsiElement> resolve(@NotNull DroolsReference reference, boolean incompleteCode) {
    MyReferenceResolvePsiElementProcessor processor = new MyReferenceResolvePsiElementProcessor(reference.getText());
    if (isDroolsQualifiedIdentifier(reference)) {
      processQualifiedIdentifier(processor, reference);
    }
    else {
      processSimplePackageOrClass(processor, reference);
      Collection<PsiElement> results = processor.getResults();
      if (results.size() > 0) return results;
      processVariables(processor, reference, incompleteCode);
    }
    return processor.getResults();
  }

  private static boolean isDroolsQualifiedIdentifier(@NotNull DroolsReference reference) {
    return PsiTreeUtil.getParentOfType(reference, DroolsQualifiedIdentifier.class) != null;
  }

  public static boolean processVariables(@NotNull CollectProcessor<PsiElement> processor,
                                         @NotNull DroolsReference reference,
                                         boolean incompleteCode) {
    DroolsReference leftReference = getLeftReference(reference);

    if (leftReference != null) {
      final PsiElement resolve = leftReference.resolve();
      if (resolve instanceof PsiClass) {
        return processClassMembers(processor, Collections.singleton((PsiClass)resolve), true);
      }
    }

    if (leftReference != null) {
      return processClassMembers(processor, leftReference);
    }
    if (PsiTreeUtil.getParentOfType(reference, DroolsRuleStatement.class) != null) {
      if (!processModifyStatements(processor, reference)) return false;
      if (!processInsertStatements(processor, reference)) return false;
      if (!processInsertLogicalStatements(processor, reference)) return false;
      if (!processRetractStatements(processor, reference)) return false;
      if (!processUpdateStatements(processor, reference)) return false;
    }

    if (!processConstrains(processor, reference)) return false;
    if (!processPrimaryExpression(processor, reference)) return false;
    if (!processPatternBinds(processor, reference)) return false;

    if (!processQueries(processor, reference)) return false;

    if (!processFunctions(processor, reference)) return false;
    if (!processParameters(processor, reference)) return false;
    if (!processGlobalVariables(processor, reference)) return false;

    return true;
  }

  private static boolean processFunctions(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    PsiFile containingFile = reference.getContainingFile();
    if (containingFile instanceof DroolsFile) {
      for (DroolsFunctionStatement functionStatement : ((DroolsFile)containingFile).getFunctions()) {
        if (!processor.process(functionStatement)) return false;
      }
    }
    return true;
  }

  private static boolean processQueries(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    PsiFile containingFile = reference.getContainingFile();
    if (containingFile instanceof DroolsFile) {
      for (DroolsQueryStatement queryStatement : ((DroolsFile)containingFile).getQueries()) {
        if (!processor.process(queryStatement)) return false;
      }
    }
    return true;
  }

  private static boolean processGlobalVariables(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    PsiFile containingFile = reference.getContainingFile();
    if (containingFile instanceof DroolsFile) {
      for (DroolsGlobalStatement globalStatement : ((DroolsFile)containingFile).getGlobalVariables()) {
        if (!processor.process(globalStatement)) return false;
      }
    }
    return true;
  }

  private static boolean processModifyStatements(@NotNull CollectProcessor<PsiElement> processor,
                                                 DroolsReference reference) {
    final DroolsModifyRhsStatement modifyRhsStatement = PsiTreeUtil.getParentOfType(reference, DroolsModifyRhsStatement.class);
    if (modifyRhsStatement != null) {
      processLocalVariables(processor, modifyRhsStatement);

      if (PsiTreeUtil.getParentOfType(reference, DroolsParExpr.class) == null) {
        PsiClass psiClass = getModifyStatementType(modifyRhsStatement);
        if (psiClass != null) {
          processClassMembers(processor, Collections.singleton(psiClass), false);
        }
      }
    }
    return true;
  }

  private static boolean processInsertStatements(@NotNull CollectProcessor<PsiElement> processor,
                                                 DroolsReference reference) {
    return processLocalVariables(processor, PsiTreeUtil.getParentOfType(reference, DroolsInsertRhsStatement.class));
  }

  private static boolean processUpdateStatements(@NotNull CollectProcessor<PsiElement> processor,
                                                 DroolsReference reference) {
    return processLocalVariables(processor, PsiTreeUtil.getParentOfType(reference, DroolsUpdateRhsStatement.class));
  }

  private static boolean processInsertLogicalStatements(@NotNull CollectProcessor<PsiElement> processor,
                                                        DroolsReference reference) {
    return processLocalVariables(processor, PsiTreeUtil.getParentOfType(reference, DroolsInsertLogicalRhsStatement.class));
  }

  private static boolean processRetractStatements(@NotNull CollectProcessor<PsiElement> processor,
                                                  DroolsReference reference) {
    return processLocalVariables(processor, PsiTreeUtil.getParentOfType(reference, DroolsRetractRhsStatement.class));
  }

  private static boolean processLocalVariables(@NotNull CollectProcessor<PsiElement> processor,
                                               @Nullable DroolsSimpleRhsStatement rhsStatement) {
    if (rhsStatement != null) {
      final Set<PsiVariable> psiVariables = getLocalVariables(rhsStatement);
      for (PsiVariable variable : psiVariables) {
        if (!processor.process(variable)) return false;
      }
    }
    return true;
  }

  @NotNull
  private static Set<PsiVariable> getLocalVariables(PsiElement rhsStatement) {
    Set<PsiVariable> variables = new HashSet<>();

    variables.addAll(DroolsLocalVariablesProcessor.getLocalVariables(rhsStatement));
    variables.addAll(DroolsRhsImplicitAssignExpressionsProcessor.getLocalVariables(rhsStatement));

    return variables;
  }

  private static boolean processClassMembers(@NotNull CollectProcessor<PsiElement> processor, DroolsReference leftReference) {
    PsiElement resolve = leftReference.resolve();
    if (resolve instanceof DroolsLhsPatternBind) {
      final DroolsLhsPatternBind bind = (DroolsLhsPatternBind)resolve;
      return processClassMembers(processor, getPatternBindType(bind.getLhsPatternList()), false);
    }
    else if (resolve instanceof DroolsUnaryAssignExpr) {
      DroolsLhsPattern droolsLhsPattern = PsiTreeUtil.getParentOfType(resolve, DroolsLhsPattern.class);
      if (droolsLhsPattern != null) {
        PsiType psiType = ((DroolsUnaryAssignExpr)resolve).getType();
        if (psiType instanceof PsiClassType) {
          return processClassMembers(processor, Collections.singleton(((PsiClassType)psiType).resolve()), false);
        }
      }
    }
    else if (resolve instanceof PsiVariable) {
      PsiType type = ((PsiVariable)resolve).getType();
      if (type instanceof PsiClassType) {
        return processClassMembers(processor, Collections.singleton(((PsiClassType)type).resolve()), false);
      }
    }
    return true;
  }

  private static boolean processConstrains(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    final DroolsConstraint constraint = PsiTreeUtil.getParentOfType(reference, DroolsConstraint.class);
    if (constraint != null) {
      final DroolsLhsPattern lhsPatternBind = PsiTreeUtil.getParentOfType(constraint, DroolsLhsPattern.class);
      if (lhsPatternBind != null) {
        if (!processClassMembers(processor, getPatternBindType(Collections.singletonList(lhsPatternBind)), false)) return false;
      }
    }
    return true;
  }

  private static boolean processPrimaryExpression(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    final DroolsArguments droolsArguments = PsiTreeUtil.getParentOfType(reference, DroolsArguments.class);
    if (droolsArguments != null) {
      final DroolsPrimaryExpr primaryExpression = PsiTreeUtil.getParentOfType(droolsArguments, DroolsPrimaryExpr.class);
      if (primaryExpression != null) {
        final PsiType type = primaryExpression.getType();
        if (type instanceof PsiClassType) {
          if (!processClassMembers(processor, Collections.singleton(((PsiClassType)type).resolve()), false)) return false;
        }
      }
    }
    return true;
  }

  private static boolean processPatternBinds(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    Set<PsiVariable> patternBinds = DroolsLhsBindVariablesProcessor.getPatternBinds(reference);
    for (PsiVariable psiVariable : patternBinds) {
      if (!processor.process(psiVariable)) return false;
    }
    return true;
  }

  private static boolean processParameters(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    final DroolsFakePsiMethod psiMethod = PsiTreeUtil.getParentOfType(reference, DroolsFakePsiMethod.class);
    if (psiMethod != null) {
      for (PsiParameter psiParameter : psiMethod.getParameterList().getParameters()) {
        if (!processor.process(psiParameter)) return false;
      }
    }
    return true;
  }

  @NotNull
  private static Set<PsiClass> resolveBoundVariableType(DroolsLhsPattern lhsPattern) {
    DroolsQualifiedIdentifier qi = lhsPattern.getLhsPatternType().getQualifiedIdentifier();
    return resolveQualifiedIdentifier(qi);
  }

  @NotNull
  public static Set<PsiClass> resolveQualifiedIdentifier(@NotNull DroolsQualifiedIdentifier qi) {
    Set<PsiClass> psiClasses = new HashSet<>();
    DroolsReference[] identifiers = PsiTreeUtil.getChildrenOfType(qi, DroolsReference.class);
    if (identifiers != null) {
      final PsiElement resolve = chooseDroolsTypeResult(identifiers[identifiers.length - 1].multiResolve(false));
      if (resolve instanceof PsiClass) {
        psiClasses.add(new DroolsLightClass((PsiClass)resolve));
      }
    }
    return psiClasses;
  }

  public static boolean processQualifiedIdentifier(@NotNull CollectProcessor<PsiElement> processor,
                                                   @NotNull DroolsReference reference) {
    if (!isDroolsQualifiedIdentifier(reference)) return true;
    DroolsReference leftReference = getLeftReference(reference);
    if (leftReference == null) {
      if (isImportQualifier(reference.getElement())) {
        return processTopPackage(processor, reference.getProject());
      }
      return processSimplePackageOrClass(processor, reference); //
    }
    else {
      for (ResolveResult result : leftReference.multiResolve(false)) {
        PsiElement element = result.getElement();
        if (element instanceof PsiPackage) {
          for (PsiPackage subPackage : ((PsiPackage)element).getSubPackages()) {
            if (!processor.process(subPackage)) return false;
          }
          for (PsiClass psiClass : ((PsiPackage)element).getClasses()) {
            if (!processor.process(psiClass)) return false;
          }
        }
        else if (element instanceof PsiClass) {
          for (PsiClass psiClass : ((PsiClass)element).getInnerClasses()) {
            if (!processor.process(psiClass)) return false;
          }
          processClassMembers(processor, Collections.singleton((PsiClass)element), true);
        }
        else if (element instanceof BeanPropertyElement) {
          PsiType propertyType = ((BeanPropertyElement)element).getPropertyType();
          if (propertyType instanceof PsiClassType) {
            processClassMembers(processor, Collections.singleton(((PsiClassType)propertyType).resolve()), false);
          }
        }
      }
    }
    return true;
  }

  private static boolean isImportQualifier(@Nullable PsiElement element) {
    return element != null && PsiTreeUtil.getParentOfType(element, DroolsImportStatement.class) != null;
  }

  private static boolean processSimplePackageOrClass(CollectProcessor<PsiElement> processor, DroolsReference reference) {
    DroolsFile droolsFile = PsiTreeUtil.getParentOfType(reference, DroolsFile.class);
    if (droolsFile != null) {
      if (!processTopPackage(processor, reference.getProject())) return false;
      for (PsiPackage aPackage : getImportedPackages(droolsFile)) {
        if (!processPackage(processor, aPackage)) return false;
      }

      if (!processImportedClasses(droolsFile, processor)) return false;

      // process declared types
      DroolsDeclareStatement[] declarations = droolsFile.getDeclarations();
      for (DroolsDeclareStatement declaration : declarations) {
        DroolsTypeDeclaration typeDeclaration = declaration.getTypeDeclaration();
        if (typeDeclaration != null && !processor.process(typeDeclaration)) {
          return false;
        }
      }
    }

    return true;
  }

  private static boolean processTopPackage(CollectProcessor<PsiElement> processor, Project project) {
    final PsiPackage top = JavaPsiFacade.getInstance(project).findPackage("");
    if (top != null) {
      for (PsiPackage aPackage : top.getSubPackages()) {
        if (!processPackage(processor, aPackage, false)) return false;
      }
    }
    return true;
  }

  private static boolean processPackage(CollectProcessor<PsiElement> processor, PsiPackage aPackage) {
    return processPackage(processor, aPackage, true);
  }

  private static boolean processPackage(CollectProcessor<PsiElement> processor, PsiPackage aPackage, boolean processClasses) {
    if (!processor.process(aPackage)) return false;
    if (processClasses) {
      for (PsiClass psiClass : aPackage.getClasses()) {
        if (!processor.process(psiClass)) return false;
      }
    }
    return true;
  }


  @Nullable
  public static DroolsReference getLeftReference(@Nullable final PsiElement node) {
    if (node == null) return null;
    for (PsiElement sibling = getPrevSiblingSkipWhiteSpaces(node, true);
         sibling != null;
         sibling = getPrevSiblingSkipWhiteSpaces(sibling, true)) {
      if (".".equals(sibling.getText())) continue;
      return sibling instanceof DroolsReference && sibling != node ? (DroolsReference)sibling : null;
    }
    return null;
  }


  @Nullable
  public static PsiElement getPrevSiblingSkipWhiteSpaces(@Nullable PsiElement sibling, boolean strictly) {
    return getPrevSiblingSkipingCondition(sibling, element -> element instanceof PsiWhiteSpace, strictly);
  }

  @Nullable
  public static PsiElement getPrevSiblingSkipingCondition(@Nullable PsiElement sibling,
                                                          Condition<? super PsiElement> condition,
                                                          boolean strictly) {
    if (sibling == null) return null;
    PsiElement result = strictly ? sibling.getPrevSibling() : sibling;
    while (result != null && condition.value(result)) {
      result = result.getPrevSibling();
    }
    return result;
  }

  private static boolean processClassMembers(@NotNull CollectProcessor<PsiElement> processor,
                                             @NotNull Set<PsiClass> psiClasses, boolean isStatic) {

    for (PsiClass psiClass : psiClasses) {
      if (psiClass == null) continue;
      if (isStatic) {
        for (PsiField field : psiClass.getAllFields()) {
          final PsiModifierList modifierList = field.getModifierList();
          if (modifierList != null
              && modifierList.hasModifierProperty(PsiModifier.PUBLIC)
              && modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            if (!processor.process(field)) return false;
          }
        }
        for (PsiMethod method : psiClass.getAllMethods()) {
          final PsiModifierList modifierList = method.getModifierList();
          if (modifierList.hasModifierProperty(PsiModifier.PUBLIC) && modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            if (!processor.process(method)) return false;
          }
        }
      }
      else {
        for (PsiMethod method : psiClass.getAllMethods()) {
          if (!processor.process(method)) return false;
        }
        for (PsiField psiField : psiClass.getAllFields()) {
          if (!processor.process(psiField)) return false;
        }
      }
      for (PsiClass innerClass : psiClass.getInnerClasses()) {
        if (!processor.process(new DroolsLightClass(innerClass))) return false;
      }
    }
    return true;
  }

  @NotNull
  public static Set<PsiClass> getPatternBindType(@NotNull Collection<? extends DroolsLhsPattern> patternBinds) {
    Set<PsiClass> psiClasses = new HashSet<>();
    for (DroolsLhsPattern lhsPattern : patternBinds) {
      psiClasses.addAll(resolveBoundVariableType(lhsPattern));
    }
    return psiClasses;
  }

  @Nullable
  public static PsiClass getModifyStatementType(@NotNull DroolsModifyRhsStatement modifyRhsStatement) {
    final DroolsExpression expression = ContainerUtil.getFirstItem(modifyRhsStatement.getExpressionList());

    final Ref<PsiClass> ref = new Ref<>(null);
    if (expression instanceof DroolsParExpr) {
      expression.acceptChildren(new DroolsVisitor() {
        @Override
        public void visitPrimaryExpr(@NotNull DroolsPrimaryExpr droolsPrimary) {
          ref.set(getPrimaryExprType(droolsPrimary));
        }

        @Override
        public void visitPsiCompositeElement(@NotNull DroolsPsiCompositeElement o) {
          o.acceptChildren(this);
        }
      });
    }
    return ref.get();
  }

  @Nullable
  private static PsiClass getPrimaryExprType(@NotNull DroolsPrimaryExpr droolsPrimary) {
    DroolsReference[] references = PsiTreeUtil.getChildrenOfType(droolsPrimary, DroolsReference.class);
    if (references != null && references.length > 0) {

      final DroolsReference reference = references[references.length - 1];
      String textToResolve = reference.getText();

      MyReferenceResolvePsiElementProcessor processor = new MyReferenceResolvePsiElementProcessor(textToResolve);

      processConstrains(processor, reference);
      processPatternBinds(processor, reference);

      for (PsiElement resolve : processor.getResults()) {
        if (resolve instanceof PsiVariable) {
          final PsiType type = ((PsiVariable)resolve).getType();

          if (type instanceof PsiClassType) {
            return ((PsiClassType)type).resolve();
          }
        }
      }
    }
    return null;
  }

  @Nullable
  public static PsiType resolveType(@Nullable DroolsType droolsType) {
    if (droolsType != null) {
      final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(droolsType.getProject());
      PsiType psiType = elementFactory.createTypeFromText(droolsType.getText(), droolsType);

      if (psiType instanceof PsiPrimitiveType) {
        return psiType;
      }
      else if (psiType instanceof PsiClassType) {
        final PsiClass psiClass = ((PsiClassType)psiType).resolve();
        if (psiClass != null) return elementFactory.createType(new DroolsLightClass(psiClass));
      }
      return resolveIdentifiers(droolsType);
    }
    return null;
  }

  @Nullable
  private static PsiType resolveIdentifiers(@NotNull final DroolsType type) {
    return RecursionManager.doPreventingRecursion(type, false, () -> {
      List<DroolsIdentifier> identifierList = type.getQualifiedIdentifier().getIdentifierList();
      final DroolsIdentifier identifier = identifierList.get(identifierList.size() - 1);
      final PsiElement resolve = identifier.resolve();
      if (resolve instanceof PsiClass) {
        return JavaPsiFacade.getElementFactory(type.getProject()).createType(new DroolsLightClass((PsiClass)resolve));
      }
      return null;
    });
  }

  public static Set<PsiPackage> getImportedPackages(@NotNull DroolsFile droolsFile) {
    return getImportedPackages(droolsFile, true);
  }

  public static Set<PsiPackage> getImportedPackages(@NotNull DroolsFile droolsFile, boolean addDefaultPackages) {
    // todo cache it
    Set<PsiPackage> imported = new HashSet<>();

    if (addDefaultPackages) imported.addAll(getDefaultPackages(droolsFile));

    imported.addAll(getExplicitlyImportedPackages(droolsFile));

    return imported;
  }

  @NotNull
  public static Set<PsiPackage> getExplicitlyImportedPackages(DroolsFile droolsFile) {
    Set<PsiPackage> imported = new HashSet<>();
    JavaPsiFacade facade = JavaPsiFacade.getInstance(droolsFile.getProject());
    for (DroolsImport droolsImport : droolsFile.getImports()) {
      String importedPackage = droolsImport.getImportedPackage();
      if (importedPackage != null) {
        addNotNull(imported, facade.findPackage(importedPackage));
      }
    }
    return imported;
  }

  @NotNull
  public static Set<PsiPackage> getDefaultPackages(DroolsFile droolsFile) {
    Set<PsiPackage> imported = new HashSet<>();
    JavaPsiFacade facade = JavaPsiFacade.getInstance(droolsFile.getProject());

    addNotNull(imported, facade.findPackage("java.lang"));
    addNotNull(imported, facade.findPackage(""));

    String packageName = getCurrentPackage(droolsFile);
    if (!StringUtil.isEmptyOrSpaces(packageName)) {
      addNotNull(imported, facade.findPackage(packageName));
    }

    return imported;
  }

  @NotNull
  public static String getCurrentPackage(@Nullable DroolsFile droolsFile) {
    if (droolsFile == null) return "";
    DroolsPackageStatement packageStatement = droolsFile.getPackage();
    return packageStatement != null ? packageStatement.getNamespace().getText() : "";
  }

  private static void addNotNull(@NotNull Set<PsiPackage> imported, @Nullable PsiPackage currentPackage) {
    if (currentPackage != null) {
      imported.add(currentPackage);
    }
  }

  @NotNull
  public static Set<PsiVariable> getVariables(@NotNull PsiElement place) {
    Set<PsiVariable> variables = new HashSet<>();
    final PsiFile file = place.getContainingFile();
    if (file instanceof DroolsFile) {
      variables.addAll(Arrays.asList(((DroolsFile)file).getGlobalVariables()));
    }
    variables.addAll(DroolsLhsBindVariablesProcessor.getPatternBinds(place));
    variables.addAll(getLocalVariables(place));

    return variables;
  }

  private static class MyReferenceResolvePsiElementProcessor extends CollectProcessor<PsiElement> {
    private final String myTextToResolve;

    MyReferenceResolvePsiElementProcessor(String textToResolve) {
      super(new HashSet<>());
      myTextToResolve = textToResolve;
    }

    @Override
    public boolean accept(PsiElement psiElement) {
      if (psiElement instanceof PsiPackage) {
        return myTextToResolve.equals(((PsiPackage)psiElement).getName());
      }
      else if (psiElement instanceof PsiClass) {
        return myTextToResolve.equals(((PsiClass)psiElement).getName());
      }
      else if (psiElement instanceof PsiField) {
        return myTextToResolve.equals(((PsiField)psiElement).getName());
      }
      else if (psiElement instanceof PsiMethod) {
        return myTextToResolve.equals(((PsiMethod)psiElement).getName());
      }
      else if (psiElement instanceof PsiVariable) {
        return myTextToResolve.equals(((PsiVariable)psiElement).getName());
      }

      return false;
    }

    @Override
    public boolean process(PsiElement psiElement) {
      if (psiElement instanceof PsiMethod) {
        final PsiMethod psiMethod = (PsiMethod)psiElement;
        if (myTextToResolve.equals(psiMethod.getName())) {
          getResults().add(psiMethod);
        }
        else {
          if (!(psiMethod instanceof DroolsPsiClassImpl.GeneratedLightMethod) && PropertyUtilBase.isSimplePropertyGetter(psiMethod)) {
            final BeanProperty property = BeanProperty.createBeanProperty(psiMethod);
            if (property != null && myTextToResolve.equals(property.getName())) {
              getResults().add(property.getPsiElement());
            }
          }
        }
      }
      else if (psiElement instanceof PsiField) {
        final PsiField psiField = (PsiField)psiElement;
        if (myTextToResolve.equals(psiField.getName())) {
          getResults().add(psiField);
        }
      }
      return super.process(psiElement);
    }
  }


  public static List<PsiClass> getExplicitlyImportedClasses(@NotNull DroolsFile droolsFile) {
    final Set<PsiElement> imported = new HashSet<>();
    CollectProcessor<PsiElement> processor = new CollectProcessor<>(imported);

    for (PsiPackage aPackage : getImportedPackages(droolsFile, false)) {
      for (PsiClass psiClass : aPackage.getClasses()) {
        processor.process(psiClass);
      }
    }

    processImportedClasses(droolsFile, processor);

    return ContainerUtil.mapNotNull(imported, psiElement -> psiElement instanceof PsiClass ? (PsiClass)psiElement : null);
  }

  public static boolean processImportedClasses(@NotNull DroolsFile droolsFile, @NotNull Processor<? super PsiElement> processor) {
    final DroolsImport[] imports = droolsFile.getImports();
    JavaPsiFacade facade = JavaPsiFacade.getInstance(droolsFile.getProject());

    for (DroolsImport droolsImport : imports) {
      String className = droolsImport.getImportedClassName();
      if (className != null) {
        PsiClass psiClass = facade.findClass(className, GlobalSearchScope.allScope(droolsFile.getProject()));
        if (psiClass != null) {
          DroolsLightClass droolsLightClass = new DroolsLightClass(psiClass);
          if (!processor.process(droolsLightClass)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public static PsiElement chooseDroolsTypeResult(ResolveResult[] resolveResults) {
    PsiElement resultElement = null;
    for (ResolveResult result : resolveResults) {
      if (result.isValidResult()) {
        final PsiElement element = result.getElement();
        if (resultElement == null) {
          resultElement = element;
        }
        else if (element instanceof PsiClass && resultElement instanceof DroolsTypeDeclaration) {
          // choose real-class, if  declared type(DroolsTypeDeclaration) is metadata provider for existing class
          // docs: 7.7.3. Declaring Metadata for Existing Types
          if (((PsiClass)element).getName().equals(((DroolsTypeDeclaration)resultElement).getName())) {
            resultElement = element;
          }
        }
      }
    }
    return resultElement;
  }
}
