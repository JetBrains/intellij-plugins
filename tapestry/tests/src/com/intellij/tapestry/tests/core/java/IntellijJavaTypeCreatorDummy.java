package com.intellij.tapestry.tests.core.java;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiImportList;
import com.intellij.tapestry.intellij.core.java.IntellijJavaTypeCreator;
import com.intellij.util.IncorrectOperationException;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class IntellijJavaTypeCreatorDummy extends IntellijJavaTypeCreator {

    private IntellijJavaTypeCreator _controlMock;

    public IntellijJavaTypeCreatorDummy(Module module) {
        super(module);
    }

    public IntellijJavaTypeCreatorDummy(Module module, IntellijJavaTypeCreator controlMock) {
        super(module);

        _controlMock = controlMock;
    }

    @Override
    public void addImport(PsiImportList importList, PsiClass aClass) throws IncorrectOperationException {
        _controlMock.addImport(importList, aClass);
    }
}
