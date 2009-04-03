package com.intellij.tapestry.tests.mocks;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageDialect;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class PsiFileMock implements PsiFile {

    private VirtualFile _virtualFile;
    private boolean _valid;

    @Nullable
    public VirtualFile getVirtualFile() {
        return _virtualFile;
    }

    public PsiFileMock setVirtualFile(VirtualFile virtualFile) {
        _virtualFile = virtualFile;

        return this;
    }

    @Nullable
    public PsiDirectory getContainingDirectory() {
        return null;
    }

    public long getModificationStamp() {
        return 0;
    }

    @Nullable
    public PsiFile getOriginalFile() {
        return null;
    }

    @NotNull
    public FileType getFileType() {
        return null;
    }

    @NotNull
    public PsiFile[] getPsiRoots() {
        return new PsiFile[0];
    }

    @NotNull
    public FileViewProvider getViewProvider() {
        return null;
    }

    public ASTNode getNode() {
        return null;
    }

    public boolean isEquivalentTo(PsiElement another) {
        return this == another;
    }

  public void subtreeChanged() {
    }

    public void checkSetName(String name) throws IncorrectOperationException {
    }

    @NotNull
    public String getName() {
        return null;
    }

    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null;
    }

    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        return null;
    }

    @NotNull
    public Language getLanguage() {
        return null;
    }

    public PsiManager getManager() {
        return null;
    }

    @NotNull
    public PsiElement[] getChildren() {
        return new PsiElement[0];
    }

    public PsiDirectory getParent() {
        return null;
    }

    @Nullable
    public PsiElement getFirstChild() {
        return null;
    }

    @Nullable
    public PsiElement getLastChild() {
        return null;
    }

    @Nullable
    public PsiElement getNextSibling() {
        return null;
    }

    @Nullable
    public PsiElement getPrevSibling() {
        return null;
    }

    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return null;
    }

    public TextRange getTextRange() {
        return null;
    }

    public int getStartOffsetInParent() {
        return 0;
    }

    public int getTextLength() {
        return 0;
    }

    @Nullable
    public PsiElement findElementAt(int offset) {
        return null;
    }

    @Nullable
    public PsiReference findReferenceAt(int offset) {
        return null;
    }

    public int getTextOffset() {
        return 0;
    }

    @NonNls
    public String getText() {
        return null;
    }

    @NotNull
    public char[] textToCharArray() {
        return new char[0];
    }

    public PsiElement getNavigationElement() {
        return null;
    }

    public PsiElement getOriginalElement() {
        return null;
    }

    public boolean textMatches(@NotNull CharSequence text) {
        return false;
    }

    public boolean textMatches(@NotNull PsiElement element) {
        return false;
    }

    public boolean textContains(char c) {
        return false;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
    }

    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
    }

    public PsiElement copy() {
        return null;
    }

    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    public PsiElement addBefore(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    public PsiElement addAfter(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
    }

    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        return null;
    }

    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    public void delete() throws IncorrectOperationException {
    }

    public void checkDelete() throws IncorrectOperationException {
    }

    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
    }

    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return null;
    }

    public boolean isValid() {
        return _valid;
    }

    public PsiFileMock setValid(boolean valid) {
        _valid = valid;

        return this;
    }

    public boolean isWritable() {
        return false;
    }

    @Nullable
    public PsiReference getReference() {
        return null;
    }

    @NotNull
    public PsiReference[] getReferences() {
        return new PsiReference[0];
    }

    @Nullable
    public <T> T getCopyableUserData(Key<T> key) {
        return null;
    }

    public <T> void putCopyableUserData(Key<T> key, T value) {
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    public PsiElement getContext() {
        return null;
    }

    public boolean isPhysical() {
        return false;
    }

    @NotNull
    public GlobalSearchScope getResolveScope() {
        return null;
    }

    @NotNull
    public SearchScope getUseScope() {
        return null;
    }

    public <T> T getUserData(Key<T> key) {
        return null;
    }

    public <T> void putUserData(Key<T> key, T value) {
    }

    public Icon getIcon(int flags) {
        return null;
    }

    @Nullable
    public ItemPresentation getPresentation() {
        return null;
    }

    public FileStatus getFileStatus() {
        return null;
    }

    public void navigate(boolean requestFocus) {
    }

    public boolean canNavigate() {
        return false;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    @Nullable
    public LanguageDialect getLanguageDialect() {
        return null;
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean processChildren(PsiElementProcessor<PsiFileSystemItem> processor) {
        return false;
    }
}
