package com.intellij.tapestry.intellij.view;

import com.intellij.ide.SelectInContext;
import com.intellij.ide.impl.ProjectViewSelectInTarget;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileSystemItem;

public class TapestryProjectSelectInTarget extends ProjectViewSelectInTarget {

    protected TapestryProjectSelectInTarget(final Project project) {
        super(project);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Tapestry Project View";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canSelect(PsiFileSystemItem psiFileSystemItem) {
        if (!super.canSelect(psiFileSystemItem)) return false;
        return TapestryProjectViewPane.getInstance(myProject).canSelect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMinorViewId() {
        return TapestryProjectViewPane.getInstance(myProject).getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWeight() {
        return 5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubIdSelectable(String subId, SelectInContext context) {
        return true;
    }
}
