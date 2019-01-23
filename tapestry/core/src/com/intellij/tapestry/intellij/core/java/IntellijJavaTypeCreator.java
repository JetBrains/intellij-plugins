package com.intellij.tapestry.intellij.core.java;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.util.ClassUtil;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaTypeCreator;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.psiutils.ImportUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class IntellijJavaTypeCreator implements IJavaTypeCreator {

    private final static Logger _logger = Logger.getInstance(IntellijJavaTypeCreator.class.getName());

    private final Module _module;

    public IntellijJavaTypeCreator(Module module) {
        _module = module;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJavaField createField(@NotNull String name, IJavaClassType type, boolean isPrivate, boolean changeNameToReflectIdeSettings) {
        String fieldName;
        if (changeNameToReflectIdeSettings) {
            fieldName = JavaCodeStyleManager.getInstance(_module.getProject()).propertyNameToVariableName(StringUtil.decapitalize(name), VariableKind.FIELD);
        } else {
            fieldName = name;
        }

        try {
            PsiField field = JavaPsiFacade.getInstance(_module.getProject()).getElementFactory().
                    createField(fieldName, JavaPsiFacade.getInstance(_module.getProject()).getElementFactory().createType(((IntellijJavaClassType) type).getPsiClass()));

            field.getModifierList().setModifierProperty(PsiModifier.PRIVATE, isPrivate);

            return new IntellijJavaField(_module, field);
        } catch (Throwable ex) {
            _logger.error(ex);

            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJavaAnnotation createFieldAnnotation(IJavaField field, String fullyQualifiedName, Map<String, String> parameters) {
        StringBuilder annotationText = new StringBuilder();
        annotationText.append("@").append(fullyQualifiedName);
        if (parameters.size() > 0) {
            annotationText.append("(");
        }

        for (String parameterName : parameters.keySet())
            annotationText.append(parameterName).append("=").append(parameters.get(parameterName).startsWith("{") ? "" : "\"").
                    append(parameters.get(parameterName)).append(parameters.get(parameterName).startsWith("{") ? "" : "\"").append(",");

        if (parameters.size() > 0) {
            annotationText.deleteCharAt(annotationText.length() - 1);
            annotationText.append(")");
        }

        try {
            PsiAnnotation annotation = JavaPsiFacade.getInstance(_module.getProject()).getElementFactory().createAnnotationFromText(annotationText.toString(), ((IntellijJavaField) field).getPsiField());
            ((IntellijJavaField) field).getPsiField().getModifierList().addBefore(annotation, ((IntellijJavaField) field).getPsiField().getModifierList().getFirstChild());

            CodeStyleManager.getInstance(_module.getProject()).reformat(((IntellijJavaField) field).getPsiField());

            return new IntellijJavaAnnotation(annotation);
        } catch (IncorrectOperationException ex) {
            _logger.error(ex);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ensureClassImport(final IJavaClassType baseClass, final IJavaClassType type) {
        if (!ImportUtils.nameCanBeImported(type.getFullyQualifiedName(), ((IntellijJavaClassType) baseClass).getPsiClass().getContainingFile())) {
            return false;
        }

        final PsiImportList importList = ((PsiJavaFile) ((IntellijJavaClassType) baseClass).getPsiClass().getContainingFile()).getImportList();
        if (importList == null) {
            return false;
        }

        String packageName = ClassUtil.extractPackageName(type.getFullyQualifiedName());
        if (packageName.equals("java.lang")) {
            if (ImportUtils.hasOnDemandImportConflict(type.getFullyQualifiedName(),
                                                      ((IntellijJavaClassType) baseClass).getPsiClass().getContainingFile())) {

                IdeaUtils.runWriteCommand(
                  null, () -> {
                      try {
                          addImport(importList, ((IntellijJavaClassType) type).getPsiClass());
                      } catch (IncorrectOperationException ex) {
                          _logger.error(ex);
                      }
                  }
                );

                PsiDocumentManager.getInstance(_module.getProject())
                        .doPostponedOperationsAndUnblockDocument(FileEditorManager.getInstance(_module.getProject()).getSelectedTextEditor().getDocument());
                return true;
            }
        } else if (importList.findSingleClassImportStatement(type.getFullyQualifiedName()) == null) {
            IdeaUtils.runWriteCommand(
              null, () -> {
                  try {
                      addImport(importList, ((IntellijJavaClassType) type).getPsiClass());
                  } catch (IncorrectOperationException ex) {
                      _logger.error(ex);
                  }
              }
            );

            if (FileEditorManager.getInstance(_module.getProject()).getSelectedFiles().length > 0) {
                PsiDocumentManager.getInstance(_module.getProject())
                        .doPostponedOperationsAndUnblockDocument(FileEditorManager.getInstance(_module.getProject()).getSelectedTextEditor().getDocument());
            }

            return true;
        } else {
            return true;
        }

        return false;
    }

    /**
     * Adds an import statement list to a class.
     *
     * @param importList the import statements to add.
     * @param aClass     the class to add the statements to.
     * @throws IncorrectOperationException if an error occurs.
     */
    public void addImport(PsiImportList importList, PsiClass aClass) throws IncorrectOperationException {
        PsiElementFactory elementFactory = JavaPsiFacade.getInstance(importList.getProject()).getElementFactory();
        PsiImportStatement importStatement = elementFactory.createImportStatement(aClass);
        importList.add(importStatement);
    }
}
