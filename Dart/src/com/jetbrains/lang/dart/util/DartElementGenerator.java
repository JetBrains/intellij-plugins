package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.DartExpressionCodeFragmentImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartElementGenerator {
  @Nullable
  public static DartReference createReferenceFromText(Project myProject, String text) {
    final DartExpression expression = createExpressionFromText(myProject, text);
    return expression instanceof DartReference ? (DartReference)expression : null;
  }

  @Nullable
  public static DartExpression createExpressionFromText(Project myProject, String text) {
    final PsiFile file = createDummyFile(myProject, "var dummy = " + text + ";");
    final PsiElement child = file.getFirstChild();
    if (child instanceof DartVarDeclarationList) {
      final DartVarInit varInit = ((DartVarDeclarationList)child).getVarInit();
      return varInit == null ? null : varInit.getExpression();
    }
    return null;
  }

  public static PsiFile createExpressionCodeFragment(Project myProject, String text, PsiElement context, boolean resolveScope) {
    final String name = "dummy." + DartFileType.DEFAULT_EXTENSION;
    final DartExpressionCodeFragmentImpl codeFragment = new DartExpressionCodeFragmentImpl(myProject, name, text, true);
    codeFragment.setContext(context);
    return codeFragment;
  }

  @Nullable
  public static PsiElement createStatementFromText(Project myProject, String text) {
    final PsiFile file = createDummyFile(myProject, "dummy(){" + text + "}");
    final PsiElement child = file.getFirstChild();
    if (child instanceof DartFunctionDeclarationWithBodyOrNative) {
      final DartFunctionBody functionBody = ((DartFunctionDeclarationWithBodyOrNative)child).getFunctionBody();
      final DartBlock block = PsiTreeUtil.getChildOfType(functionBody, DartBlock.class);
      final DartStatements statements = block == null ? null : block.getStatements();
      return statements == null ? null : statements.getFirstChild();
    }
    return null;
  }


  public static List<DartComponent> createFunctionsFromText(Project myProject, String text) {
    final PsiFile dummyFile = createDummyFile(myProject, DartCodeGenerateUtil.wrapFunction(text).getFirst());
    final DartClass dartClass = PsiTreeUtil.getChildOfType(dummyFile, DartClass.class);
    assert dartClass != null;
    return DartResolveUtil.findNamedSubComponents(false, dartClass);
  }

  @Nullable
  public static DartId createIdentifierFromText(Project myProject, String name) {
    final PsiFile dummyFile = createDummyFile(myProject, name + "(){}");
    final DartComponent dartComponent = PsiTreeUtil.getChildOfType(dummyFile, DartComponent.class);
    final DartComponentName componentName = dartComponent == null ? null : dartComponent.getComponentName();
    return componentName == null ? null : componentName.getId();
  }

  @Nullable
  public static DartQualifiedComponentName createQIdentifierFromText(Project myProject, String name) {
    final PsiFile dummyFile = createDummyFile(myProject, "library " + name + ";");
    final DartLibraryStatement libraryStatement = PsiTreeUtil.getChildOfType(dummyFile, DartLibraryStatement.class);
    return libraryStatement == null ? null : libraryStatement.getQualifiedComponentName();
  }

  @Nullable
  public static DartLibraryComponentReferenceExpression createLibraryComponentReference(Project myProject, String name) {
    final PsiFile dummyFile = createDummyFile(myProject, "import 'dummy' show " + name + ";");
    final DartImportStatement importStatement = PsiTreeUtil.getChildOfType(dummyFile, DartImportStatement.class);
    final List<DartShowCombinator> combinators = importStatement != null ? importStatement.getShowCombinatorList() : null;
    final DartLibraryReferenceList libraryReferences = combinators != null && !combinators.isEmpty() ?
                                                       combinators.iterator().next().getLibraryReferenceList() : null;
    final List<DartLibraryComponentReferenceExpression> references = libraryReferences != null ?
                                                                     libraryReferences.getLibraryComponentReferenceExpressionList() : null;
    return references == null ? null : references.iterator().next();
  }

  @Nullable
  public static DartSourceStatement createSourceStatementFromPath(Project myProject, String path) {
    final PsiFile dummyFile = createDummyFile(myProject, "#source(" + path + ");");
    return PsiTreeUtil.getChildOfType(dummyFile, DartSourceStatement.class);
  }

  @Nullable
  public static PsiElement createTopLevelStatementFromText(Project myProject, String text) {
    final PsiFile file = createDummyFile(myProject, text);
    return file.getFirstChild();
  }

  public static PsiFile createDummyFile(Project myProject, String text) {
    final PsiFileFactory factory = PsiFileFactory.getInstance(myProject);
    final String name = "dummy." + DartFileType.INSTANCE.getDefaultExtension();
    final LightVirtualFile virtualFile = new LightVirtualFile(name, DartFileType.INSTANCE, text);
    final PsiFile psiFile = ((PsiFileFactoryImpl)factory).trySetupPsiForFile(virtualFile, DartLanguage.INSTANCE, false, true);
    assert psiFile != null;
    return psiFile;
  }
}
