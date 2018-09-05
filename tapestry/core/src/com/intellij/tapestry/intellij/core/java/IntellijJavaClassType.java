package com.intellij.tapestry.intellij.core.java;

import com.intellij.codeInsight.AnnotationUtil;
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
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IntellijJavaClassType extends IntellijJavaType implements IJavaClassType {
  @NotNull
  private final String _classFilePath;
  private PsiClassType _psiClassType;
  private final Module _module;
  private Boolean _supportInformalParameters;

  public IntellijJavaClassType(Module module, PsiFile psiFile) {
    _module = module;
    VirtualFile virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) virtualFile = psiFile.getViewProvider().getVirtualFile();
    _classFilePath = virtualFile.getUrl();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFullyQualifiedName() {
    return getPsiClass().getQualifiedName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return getPsiClass().getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isInterface() {
    return getPsiClass().isInterface();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPublic() {
    return getPsiClass().getModifierList().hasModifierProperty(PsiModifier.PUBLIC);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEnum() {
    return getPsiClass().isEnum();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public IntellijJavaClassType getSuperClassType() {
    PsiClass superClass = getPsiClass().getSuperClass();
    return superClass != null ? new IntellijJavaClassType(_module, superClass.getContainingFile()) : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasDefaultConstructor() {
    return PsiUtil.hasDefaultConstructor(getPsiClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IJavaMethod> getPublicMethods(boolean fromSuper) {
    Collection<IJavaMethod> foundMethods = new ArrayList<>();

    for (PsiMethod method : getMethods(fromSuper)) {
      if (method.getModifierList().hasExplicitModifier(PsiModifier.PUBLIC) && isNotMethodOfJavaLangObject(method)) {
        foundMethods.add(new IntellijJavaMethod(_module, method));
      }
    }

    return foundMethods;
  }

  private boolean isNotMethodOfJavaLangObject(PsiMethod method) {
    return !method.getContainingClass().getQualifiedName().equals(CommonClassNames.JAVA_LANG_OBJECT);
  }

  private PsiMethod[] getMethods(boolean fromSuper) {
    return fromSuper ? getPsiClass().getAllMethods() : getPsiClass().getMethods();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IJavaMethod> getAllMethods(boolean fromSuper) {
    Collection<IJavaMethod> foundMethods = new ArrayList<>();

    for (PsiMethod method : getMethods(fromSuper)) {
      if (isNotMethodOfJavaLangObject(method)) {
        foundMethods.add(new IntellijJavaMethod(_module, method));
      }
    }

    return foundMethods;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<IJavaMethod> findPublicMethods(String methodNameRegExp) {
    RegExpValidator regexpValidator = new RegExpValidator(methodNameRegExp);
    Collection<IJavaMethod> foundMethods = new ArrayList<>();

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
  @Override
  public Collection<IJavaAnnotation> getAnnotations() {
    Collection<IJavaAnnotation> annotations = new ArrayList<>();

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
  @Override
  public Map<String, IJavaField> getFields(boolean fromSuper) {
    Map<String, IJavaField> fields = new HashMap<>();

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
  @Override
  public String getDocumentation() {
    StringBuilder description = new StringBuilder();

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
  @Override
  public IResource getFile() {
    VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(_classFilePath);

    if (vf != null) {
      return new IntellijResource(PsiManager.getInstance(_module.getProject()).findFile(vf));
    }
    else {
      return null;
    }
  }

  private static final String INFORMAL_PARAMETERS_ANNOTATION = "org.apache.tapestry5.annotations.SupportsInformalParameters";

  @Override
  public boolean supportsInformalParameters() {
    if (_supportInformalParameters == null) {
      boolean result = false;
      PsiClass psiClass = getPsiClass();
      if (psiClass != null && AnnotationUtil.isAnnotated(psiClass, INFORMAL_PARAMETERS_ANNOTATION, AnnotationUtil.CHECK_HIERARCHY)) {
        result = true;
      }
      _supportInformalParameters = result;
    }
    return _supportInformalParameters;
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
    return res;
  }


  @Override
  @NotNull
  public Object getUnderlyingObject() {
    if (_psiClassType == null) processPsiClassType();
    return _psiClassType;
  }

  private void processPsiClassType() {
    final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(_classFilePath);
    if (file == null) {
      _psiClassType = null;
      return;
    }

    PsiFile psiFile = PsiManager.getInstance(_module.getProject()).findFile(file);
    if (psiFile instanceof PsiClassOwner) {
      PsiClass[] psiClasses = ((PsiClassOwner)psiFile).getClasses();
      PsiClass aClass = IdeaUtils.findPublicClass(psiClasses);
      if (aClass == null && psiClasses.length > 0) aClass = psiClasses[0];

      if (aClass != null) {
        _psiClassType = JavaPsiFacade.getInstance(_module.getProject()).getElementFactory().createType(aClass);
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
