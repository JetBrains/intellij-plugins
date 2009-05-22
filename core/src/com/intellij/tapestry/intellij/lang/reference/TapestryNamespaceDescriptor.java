package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.java.PsiEmptyExpressionImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.XmlElementDescriptorImpl;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;

public class TapestryNamespaceDescriptor extends XmlNSDescriptorImpl {

    private static final PsiElement EMPTY_PSI_ELEMENT = new PsiEmptyExpressionImpl() {
        public PsiFile getContainingFile() {
            return null;
        }
    };

    public XmlElementDescriptor getElementDescriptor(@NotNull XmlTag tag) {
        return new TapestryElementDescriptor(tag);
    }

    public boolean isHierarhyEnabled() {
        return true;
    }

    private class TapestryElementDescriptor extends XmlElementDescriptorImpl {

        private XmlTag _tag;

        public TapestryElementDescriptor(XmlTag tag) {
            super(tag);

            _tag = tag;
        }

        public PsiElement getDeclaration() {
            VirtualFile tagFile = _tag.getContainingFile().getVirtualFile();
            if (tagFile == null) {
                tagFile = _tag.getContainingFile().getOriginalFile().getVirtualFile();
            }

          if (!tagFile.isInLocalFileSystem()) return EMPTY_PSI_ELEMENT;
          Component component = TapestryUtils.getComponentFromTag(ProjectRootManager.getInstance(_tag.getProject()).getFileIndex().getModuleForFile(tagFile), _tag);
          if(component == null) return EMPTY_PSI_ELEMENT;

          final IJavaClassType classType = component.getElementClass();
          return classType == null ? EMPTY_PSI_ELEMENT : ((IntellijJavaClassType)classType).getPsiClass();
        }

        public XmlElementDescriptor getElementDescriptor(XmlTag xmlTag) {
            return new TapestryElementDescriptor(xmlTag);
        }

        public String getName(PsiElement psiElement) {
            return _tag.getName();
        }

        public String getName() {
            return _tag.getName();
        }
    }
}
