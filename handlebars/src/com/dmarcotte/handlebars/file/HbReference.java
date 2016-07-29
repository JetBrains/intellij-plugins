package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.psi.HbPsiElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class HbReference implements PsiReference {
    PsiElement element;
    TextRange textRange;
    VirtualFile projectBaseDir;
    VirtualFile containingFile;
    String containingFileExtension;
    String[] templatesLocations;
    String partialName;

    public HbReference(PsiElement element) {
        HbPsiElement partial = (HbPsiElement) element;
        ASTNode partialNode = partial.getNode();
        Project project = partial.getProject();
        if(partial.getName() != null) {
            //this is weird...
            this.partialName = partial.getName().replace("IntellijIdeaRulezzz", "");
        } else {
            this.partialName = "";
        }

        this.element = element;
        int offset = partialNode.getText().indexOf(this.partialName);
        this.textRange =  new TextRange(offset, this.partialName.length() + offset);
        this.projectBaseDir = project.getBaseDir();
        this.templatesLocations = HbConfig.getNormalizedTemplatesLocations();
        this.containingFile = this.element.getContainingFile().getOriginalFile().getVirtualFile();
        this.containingFileExtension = "." + this.containingFile.getExtension();
    }

    @Override
    public PsiElement getElement() {
        return this.element;
    }

    @Nullable
    @Override
    public PsiElement resolve() {

        VirtualFile file = findPartialInKnownPaths();

        if (file != null) {
            return PsiManager.getInstance(element.getProject()).findFile(file);
        }

        return null;
    }

    @Override
    public String toString() {
        return getCanonicalText();
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        ArrayList<LookupElement> completionResultSet = new ArrayList<LookupElement>();

        List<String> files = getAllFilesInDirectory(element
                .getProject()
                .getBaseDir());

        for (String file : files) {
            int indexOfLookupString = file.indexOf(this.partialName);
            String partialPresentationString = file;

            if(indexOfLookupString != -1) {
                String suggestedPartialName = file.substring(indexOfLookupString);
                String partialPath = file.substring(0, indexOfLookupString);

                partialPresentationString = suggestedPartialName + " (" + partialPath + ")";
            }

            completionResultSet.add(
                    LookupElementBuilder
                            .create(element, file)
                            .withInsertHandler(
                                    HbInsertHandler.getInstance()
                            )
                            .withPresentableText(partialPresentationString)
            );
        }

        return completionResultSet.toArray();
    }

    @Override
    public boolean isReferenceTo(PsiElement psiElement) {
        return false;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public PsiElement handleElementRename(String s) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public TextRange getRangeInElement() {
        return textRange;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return element.getText();
    }

    public List<String> getAllFilesInDirectory(VirtualFile directory) {
        List<String> files = new ArrayList<String>();
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child instanceof VirtualDirectoryImpl && ((VirtualDirectoryImpl) child).isFileIndexed()) {
                files.addAll(getAllFilesInDirectory(child));
            } else if (child instanceof VirtualFileImpl
                    && child.getFileType() instanceof HbFileType
                    && child.getPath().contains("/" + this.partialName)) {
                files.add(child.getPath().replace(this.projectBaseDir.getPath(), "").replace(this.containingFileExtension, ""));
            }
        }
        return files;

    }

    private VirtualFile findPartialInKnownPaths() {
        VirtualFile file;
        for(String templatesLocation : this.templatesLocations) {
            if(this.containingFile.getPath().contains(templatesLocation)) {
                file = projectBaseDir.findFileByRelativePath(templatesLocation + getPartialFileName());

                if(file != null) {
                    return file;
                }
            }
        }

        for(String templatesLocation : this.templatesLocations) {
            file = projectBaseDir.findFileByRelativePath(templatesLocation + getPartialFileName());
            if(file != null) {
                return file;
            }
        }

        return projectBaseDir.findFileByRelativePath(getPartialFileName());
    }

    private String getPartialFileName() {
        return this.partialName + this.containingFileExtension;
    }
}
