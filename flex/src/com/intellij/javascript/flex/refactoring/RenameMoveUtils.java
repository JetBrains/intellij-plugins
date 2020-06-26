// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.refactoring;

import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.psi.impl.JSPsiImplUtils.*;

/**
 * @author Maxim.Mossienko
 */
public final class RenameMoveUtils {
  private static final Key<String> oldPackageKey = Key.create("old.package.key");

  private RenameMoveUtils() {
  }

  public static void updateFileWithChangedName(JSFile file) throws IncorrectOperationException {
    VirtualFile virtualFile = file.getVirtualFile();

    if (!ProjectRootManager.getInstance(file.getProject()).getFileIndex().isInSource(virtualFile)) {
      return;
    }

    JSQualifiedNamedElement element = findQualifiedElement(file);
    if (element != null) {
      String shortName = virtualFile.getNameWithoutExtension();
      JSFunction constructor = null;
      if (element instanceof JSClass) {
        constructor = ((JSClass)element).getConstructor();
      }
      element.setName(shortName);
      if (constructor != null) constructor.setName(shortName);
    }
  }

  public static @Nullable
  JSPackageStatement updatePackageStatement(JSFile file) {
    JSPackageStatement packageStatement = findPackageStatement(file);

    if (packageStatement != null) {
      final String s = packageStatement.getQualifiedName();
      final String expectedPackageNameFromFile = JSResolveUtil.getExpectedPackageNameFromFile(file.getVirtualFile(), file.getProject());

      if (differentPackageName(s, expectedPackageNameFromFile)) {
        packageStatement.setQualifiedName(expectedPackageNameFromFile);
      }
    }
    return packageStatement;
  }

  public static void updateMovedFile(JSFile file) throws IncorrectOperationException {
    updateMovedFile(file, file);
  }

  public static void updateMovedFile(JSFile file, UserDataHolder dataHolder) throws IncorrectOperationException {
    final JSPackageStatement statement = updatePackageStatement(file);
    if (statement != null) {
      final String qName = statement.getQualifiedName();
      final String oldPackageKey = dataHolder.getUserData(RenameMoveUtils.oldPackageKey);
      file.putUserData(RenameMoveUtils.oldPackageKey, null);

      if (differentPackageName(oldPackageKey, qName) && !isEmpty(oldPackageKey)) {
        ASTNode node;

        statement.addAfter(
            JSChangeUtil.createStatementFromText(file.getProject(), "import " + oldPackageKey + ".*;", (JSLanguageDialect)file.getLanguage()).getPsi(),
            (node = statement.getNode().findChildByType(JSTokenTypes.LBRACE)) != null ? node.getPsi():null
        );
      }
    }
  }

  public static void prepareMovedFile(final JSFile file) {
    prepareMovedFile(file, file);
  }

  public static void prepareMovedFile(final JSFile file, UserDataHolder dataHolder) {
    saveOldPackageNameCheckingRefs(file, dataHolder);
  }

  private static void saveOldPackageNameCheckingRefs(final JSFile file, UserDataHolder dataHolder) {
    final JSPackageStatement statement = findPackageStatement(file);
    if (statement == null) return;
    final String oldPackageName = statement.getQualifiedName();
    final Ref<Boolean> hasReferencesToOldPackage = new Ref<>(Boolean.FALSE);

    checkIfFileHasReferencesToOldPackage(file, oldPackageName, hasReferencesToOldPackage);

    if (hasReferencesToOldPackage.get().booleanValue()) {
      dataHolder.putUserData(oldPackageKey, oldPackageName);
    }
  }

  private static void checkIfFileHasReferencesToOldPackage(JSFile file, final String oldPackageName, final Ref<Boolean> hasReferencesToOldPackage) {
    final PsiFile realFile = getRealFile(file);
    file.acceptChildren(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (hasReferencesToOldPackage.get().booleanValue()) return;
        for(PsiReference el:element.getReferences()) {
          if (el instanceof PsiPolyVariantReference) {
            ResolveProcessor.setSkipPackageLocalCheck(element, true);
            for(ResolveResult r:((PsiPolyVariantReference)el).multiResolve(false)) {
              checkIfResolveBelongsToPackage(r.getElement(), hasReferencesToOldPackage);
            }
            ResolveProcessor.setSkipPackageLocalCheck(element, false);
          } else {
            PsiElement resolve = el.resolve();
            if (resolve != null) checkIfResolveBelongsToPackage(resolve, hasReferencesToOldPackage);
          }
        }

        if (hasReferencesToOldPackage.get().booleanValue()) return;
        super.visitElement(element);
      }

      private void checkIfResolveBelongsToPackage(PsiElement resolve, Ref<Boolean> hasReferencesToOldPackage) {
        PsiFile containingFile = getRealFile(resolve.getContainingFile());

        if(containingFile != realFile &&
           containingFile != null &&
           !differentPackageName(
             JSResolveUtil.getExpectedPackageNameFromFile(containingFile.getVirtualFile(), containingFile.getProject()),
             oldPackageName
           ) &&
           !isEmpty(oldPackageName)
          ) {
          hasReferencesToOldPackage.set(Boolean.TRUE);
        }
      }
    });
  }

  private static PsiFile getRealFile(PsiFile realFile) {
    PsiElement context = realFile != null ? realFile.getContext():null;
    if (context != null) realFile = context.getContainingFile();
    return realFile;
  }

  public static void prepareMovedMxmlFile(XmlFile xmlFile, @Nullable XmlFile originalElement) {
    prepareMovedMxmlFile(xmlFile, originalElement, xmlFile);
  }

  public static void prepareMovedMxmlFile(XmlFile xmlFile, @Nullable XmlFile originalElement, UserDataHolder dataHolder) {
    VirtualFile file = (originalElement != null ? originalElement : xmlFile).getVirtualFile();
    final String expectedPackageNameFromFile =
      JSResolveUtil.getExpectedPackageNameFromFile(file, xmlFile.getProject());

    final Ref<Boolean> hasReferencesToOldPackage = new Ref<>(Boolean.FALSE);

    xmlFile.acceptChildren(new XmlRecursiveElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (hasReferencesToOldPackage.get().booleanValue()) return;

        if (element instanceof PsiLanguageInjectionHost) {
          InjectedLanguageManager.getInstance(element.getProject()).enumerate(element, new JSResolveUtil.JSInjectedFilesVisitor() {
            @Override
            protected void process(JSFile file) {
              checkIfFileHasReferencesToOldPackage(file, expectedPackageNameFromFile, hasReferencesToOldPackage);
            }
          });
        }
        super.visitElement(element);
      }
    });

    if (hasReferencesToOldPackage.get().booleanValue()) {
      dataHolder.putUserData(oldPackageKey, expectedPackageNameFromFile);
    }
  }

  public static void updateMovedMxmlFile(XmlFile xmlFile) {
    updateMovedMxmlFile(xmlFile, xmlFile);
  }

  public static void updateMovedMxmlFile(XmlFile xmlFile, UserDataHolder dataHolder) {
    String oldPackage = dataHolder.getUserData(oldPackageKey);
    xmlFile.putUserData(oldPackageKey, null);
    String newPackage = JSResolveUtil.getExpectedPackageNameFromFile(xmlFile.getVirtualFile(), xmlFile.getProject());

    if (differentPackageName(oldPackage, newPackage) && !isEmpty(oldPackage)) {
      String fqn = oldPackage + ".*";

      for(JSClass c:XmlBackedJSClassImpl.getClasses(xmlFile)) {
        ImportUtils.doImport(c, fqn, true);
      }
    }
  }
}
