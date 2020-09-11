package com.intellij.tapestry.tests.mocks;

import com.intellij.lang.FileASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
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

    @Override
    @Nullable
    public VirtualFile getVirtualFile() {
        return _virtualFile;
    }

    public PsiFileMock setVirtualFile(VirtualFile virtualFile) {
        _virtualFile = virtualFile;

        return this;
    }

    @Override
    @Nullable
    public PsiDirectory getContainingDirectory() {
        return null;
    }

    @Override
    public long getModificationStamp() {
        return 0;
    }

    @Override
    @NotNull
    public PsiFile getOriginalFile() {
        return this;
    }

    @Override
    @NotNull
    public FileType getFileType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public PsiFile @NotNull [] getPsiRoots() {
        return PsiFile.EMPTY_ARRAY;
    }

    @Override
    @NotNull
    public FileViewProvider getViewProvider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public FileASTNode getNode() {
        return null;
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return this == another;
    }

  @Override
  public void subtreeChanged() {
    }

    @Override
    public void checkSetName(String name) throws IncorrectOperationException {
    }

    @Override
    @NotNull
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null;
    }

    @Override
    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
      throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public Language getLanguage() {
      throw new UnsupportedOperationException();
    }

    @Override
    public PsiManager getManager() {
        return null;
    }

    @Override
    public PsiElement @NotNull [] getChildren() {
        return PsiElement.EMPTY_ARRAY;
    }

    @Override
    public PsiDirectory getParent() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getFirstChild() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getLastChild() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getNextSibling() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getPrevSibling() {
        return null;
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return null;
    }

    @Override
    public TextRange getTextRange() {
        return null;
    }

    @Override
    public int getStartOffsetInParent() {
        return 0;
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Override
    @Nullable
    public PsiElement findElementAt(int offset) {
        return null;
    }

    @Override
    @Nullable
    public PsiReference findReferenceAt(int offset) {
        return null;
    }

    @Override
    public int getTextOffset() {
        return 0;
    }

    @Override
    @NonNls
    public String getText() {
        return null;
    }

    @Override
    public char @NotNull [] textToCharArray() {
        return new char[0];
    }

    @Override
    public PsiElement getNavigationElement() {
        return null;
    }

    @Override
    public PsiElement getOriginalElement() {
        return null;
    }

    @Override
    public boolean textMatches(@NotNull CharSequence text) {
        return false;
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        return false;
    }

    @Override
    public boolean textContains(char c) {
        return false;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
    }

    @Override
    public PsiElement copy() {
        return null;
    }

    @Override
    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
    }

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return null;
    }

    @Override
    public void delete() throws IncorrectOperationException {
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean isValid() {
        return _valid;
    }

    public PsiFileMock setValid(boolean valid) {
        _valid = valid;

        return this;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    @Nullable
    public PsiReference getReference() {
        return null;
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return PsiReference.EMPTY_ARRAY;
    }

    @Override
    @Nullable
    public <T> T getCopyableUserData(Key<T> key) {
        return null;
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, T value) {
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return false;
    }

    @Override
    @Nullable
    public PsiElement getContext() {
        return null;
    }

    @Override
    public boolean isPhysical() {
        return false;
    }

    @Override
    @NotNull
    public GlobalSearchScope getResolveScope() {
      throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public SearchScope getUseScope() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, T value) {
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }

    @Override
    @Nullable
    public ItemPresentation getPresentation() {
        return null;
    }

    @Override
    public void navigate(boolean requestFocus) {
    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean processChildren(@NotNull PsiElementProcessor<? super PsiFileSystemItem> processor) {
        return false;
    }
}
