package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.rename.RegExpValidator;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IntellijJavaClassType extends IntellijJavaType implements IJavaClassType {

  private static final Logger ourLogger = LoggerFactory.getInstance().getLogger(IntellijJavaClassType.class);
  @NotNull
  private final String _classFilePath;
  private PsiClassType _psiClassType;
  private Module _module;

  public IntellijJavaClassType(Module module, PsiFile psiFile) {
    _module = module;
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    _classFilePath = virtualFile.getUrl();
  }

  /**
   * {@inheritDoc}
   */
  public String getFullyQualifiedName() {
    return getPsiClass().getQualifiedName();
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return getPsiClass().getName();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isInterface() {
    return getPsiClass().isInterface();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isPublic() {
    return getPsiClass().getModifierList().hasExplicitModifier(PsiModifier.PUBLIC);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isEnum() {
    return getPsiClass().isEnum();
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public IntellijJavaClassType getSuperClassType() {
    PsiClass superClass = getPsiClass().getSuperClass();
    return superClass != null ? new IntellijJavaClassType(_module, superClass.getContainingFile()) : null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasDefaultConstructor() {
    return PsiUtil.hasDefaultConstructor(getPsiClass());
  }

  /**
   * {@inheritDoc}
   */
  public Collection<IJavaMethod> getPublicMethods(boolean fromSuper) {
    Collection<IJavaMethod> foundMethods = new ArrayList<IJavaMethod>();

    PsiMethod[] allMethods;
    if (fromSuper) {
      allMethods = getPsiClass().getAllMethods();
    }
    else {
      allMethods = getPsiClass().getMethods();
    }

    for (PsiMethod method : allMethods) {
      if (method.getModifierList().hasExplicitModifier(PsiModifier.PUBLIC) &&
          (!method.getContainingClass().getQualifiedName().equals("java.lang.Object"))) {
        foundMethods.add(new IntellijJavaMethod(_module, method));
      }
    }

    return foundMethods;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<IJavaMethod> getAllMethods(boolean fromSuper) {
    Collection<IJavaMethod> foundMethods = new ArrayList<IJavaMethod>();

    PsiMethod[] allMethods;
    if (fromSuper) {
      allMethods = getPsiClass().getAllMethods();
    }
    else {
      allMethods = getPsiClass().getMethods();
    }

    for (PsiMethod method : allMethods) {
      if (!method.getContainingClass().getQualifiedName().equals("java.lang.Object")) {
        foundMethods.add(new IntellijJavaMethod(_module, method));
      }
    }

    return foundMethods;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<IJavaMethod> findPublicMethods(String methodNameRegExp) {
    RegExpValidator regexpValidator = new RegExpValidator(methodNameRegExp);
    Collection<IJavaMethod> foundMethods = new ArrayList<IJavaMethod>();

    Collection<IJavaMethod> allMethods = getPublicMethods(true);
    for (IJavaMethod method : allMethods) {
      if (regexpValidator.value(method.getName())) {
        foundMethods.add(method);
      }
    }

    return foundMethods;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<IJavaAnnotation> getAnnotations() {
    Collection<IJavaAnnotation> annotations = new ArrayList<IJavaAnnotation>();

    PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return annotations;
    }

    for (PsiAnnotation annotation : getPsiClass().getModifierList().getAnnotations()) {
      annotations.add(new IntellijJavaAnnotation(annotation));
    }

    return annotations;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, IJavaField> getFields(boolean fromSuper) {
    Map<String, IJavaField> fields = new HashMap<String, IJavaField>();

    PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return fields;
    }

    PsiField[] classFields;
    try {
      classFields = fromSuper ? getPsiClass().getAllFields() : getPsiClass().getFields();
    }
    catch (PsiInvalidElementAccessException ex) {
      // thrown if the class is invalid, should ignore and return an empty Map
      return fields;
    }

    for (PsiField field : classFields) {
      fields.put(field.getName(), new IntellijJavaField(_module, field));
    }

    return fields;
  }

  /**
   * {@inheritDoc}
   */
  public String getDocumentation() {
    StringBuffer description = new StringBuffer();

    PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return description.toString();
    }

    PsiDocComment document = getPsiClass().getDocComment();
    if (document == null) {
      document = ((PsiClass)getPsiClass().getNavigationElement()).getDocComment();
    }

    if (document != null) {
      for (PsiElement comment : document.getDescriptionElements()) {
        if (!(comment instanceof PsiWhiteSpace)) {
          description.append(comment.getText());
        }
      }
    }

    return description.toString();
  }

  /**
   * {@inheritDoc}
   */
  public IResource getFile() {
    VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(_classFilePath);

    if (vf != null) {
      return new IntellijResource(PsiManager.getInstance(_module.getProject()).findFile(vf));
    }
    else {
      return null;
    }
  }

  /**
   * Returns the psi class associated with this class.
   *
   * @return psi class associated with this class.
   */
  @Nullable
  public PsiClass getPsiClass() {
    PsiClass res;
    if (_psiClassType != null && _psiClassType.isValid() && _psiClassType.resolve().getContainingFile().isValid()) {
      res = _psiClassType.resolve();
    }
    else {
      processPsiClassType();
      res = _psiClassType != null ? _psiClassType.resolve() : null;
    }
    if (res == null) {
      ourLogger.error((_psiClassType != null) + ", unresolved: " + _classFilePath);
    }
    return res;
  }


  @NotNull
  public Object getUnderlyingObject() {
    if (_psiClassType == null) processPsiClassType();
    return _psiClassType;
  }

  private void processPsiClassType() {
    PsiFile psiFile = PsiManager.getInstance(_module.getProject()).findFile(VirtualFileManager.getInstance().findFileByUrl(_classFilePath));

    if (psiFile instanceof PsiClassOwner) {
      PsiClass[] psiClasses = ((PsiClassOwner)psiFile).getClasses();

      if (psiClasses.length > 0) {
        _psiClassType = JavaPsiFacade.getInstance(_module.getProject()).getElementFactory().createType(psiClasses[0]);
      }
      else {
        throw new AssertionError("no classes found: " + _classFilePath);
      }
    }
    else {
      throw new AssertionError(psiFile + ": " + _classFilePath);
    }

  }
}
