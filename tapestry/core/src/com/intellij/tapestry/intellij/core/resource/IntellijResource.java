package com.intellij.tapestry.intellij.core.resource;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.resource.CoreXmlRecursiveElementVisitor;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.core.resource.xml.IntellijXmlTag;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class IntellijResource implements IResource {

    private final PsiFile _psiFile;

    public IntellijResource(@NotNull PsiFile psiFile) {
        _psiFile = psiFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _psiFile.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
        VirtualFile virtualFile = _psiFile.getViewProvider().getVirtualFile();
        return new File(virtualFile.getPath());
    }

    /**
     * {@inheritDoc}
     */
    public PsiFile getPsiFile() {
        return _psiFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExtension() {
        return _psiFile.getVirtualFile().getExtension();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(final CoreXmlRecursiveElementVisitor visitor) {
        _psiFile.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);

                visitor.visitTag(new IntellijXmlTag(tag));
            }
        });
    }
} //IntellijResource
