package com.intellij.tapestry.tests.mocks;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class PsiClassMock implements PsiClass {

    private boolean _valid;
    private PsiFile _containingFile;

    @Nullable
    @NonNls
    public String getQualifiedName() {
        return null;
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isAnnotationType() {
        return false;
    }

    public boolean isEnum() {
        return false;
    }

    @Nullable
    public PsiReferenceList getExtendsList() {
        return null;
    }

    @Nullable
    public PsiReferenceList getImplementsList() {
        return null;
    }

    @NotNull
    public PsiClassType[] getExtendsListTypes() {
        return new PsiClassType[0];
    }

    @NotNull
    public PsiClassType[] getImplementsListTypes() {
        return new PsiClassType[0];
    }

    @Nullable
    public PsiClass getSuperClass() {
        return null;
    }

    public PsiClass[] getInterfaces() {
        return new PsiClass[0];
    }

    @NotNull
    public PsiClass[] getSupers() {
        return new PsiClass[0];
    }

    @NotNull
    public PsiClassType[] getSuperTypes() {
        return new PsiClassType[0];
    }

    @NotNull
    public PsiField[] getFields() {
        return new PsiField[0];
    }

    @NotNull
    public PsiMethod[] getMethods() {
        return new PsiMethod[0];
    }

    @NotNull
    public PsiMethod[] getConstructors() {
        return new PsiMethod[0];
    }

    @NotNull
    public PsiClass[] getInnerClasses() {
        return new PsiClass[0];
    }

    @NotNull
    public PsiClassInitializer[] getInitializers() {
        return new PsiClassInitializer[0];
    }

    @NotNull
    public PsiField[] getAllFields() {
        return new PsiField[0];
    }

    @NotNull
    public PsiMethod[] getAllMethods() {
        return new PsiMethod[0];
    }

    @NotNull
    public PsiClass[] getAllInnerClasses() {
        return new PsiClass[0];
    }

    @Nullable
    public PsiField findFieldByName(String name, boolean checkBases) {
        return null;
    }

    @Nullable
    public PsiMethod findMethodBySignature(PsiMethod patternMethod, boolean checkBases) {
        return null;
    }

    @NotNull
    public PsiMethod[] findMethodsBySignature(PsiMethod patternMethod, boolean checkBases) {
        return new PsiMethod[0];
    }

    @NotNull
    public PsiMethod[] findMethodsByName(@NonNls String name, boolean checkBases) {
        return new PsiMethod[0];
    }

    @NotNull
    public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(String name, boolean checkBases) {
        return null;
    }

    @NotNull
    public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors() {
        return null;
    }

    @Nullable
    public PsiClass findInnerClassByName(String name, boolean checkBases) {
        return null;
    }

    @Nullable
    public PsiJavaToken getLBrace() {
        return null;
    }

    @Nullable
    public PsiJavaToken getRBrace() {
        return null;
    }

    @Nullable
    public PsiIdentifier getNameIdentifier() {
        return null;
    }

    public PsiElement getScope() {
        return null;
    }

    public boolean isInheritor(@NotNull PsiClass baseClass, boolean checkDeep) {
        return false;
    }

    public boolean isInheritorDeep(PsiClass baseClass, @Nullable PsiClass classToByPass) {
        return false;
    }

    //@Nullable
    //public PomMemberOwner getPom() {
    //    return null;
    //}
    //
    @Nullable
    public PsiClass getContainingClass() {
        return null;
    }

    @NotNull
    public Collection<HierarchicalMethodSignature> getVisibleSignatures() {
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

    public PsiElement getParent() {
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
        return _containingFile;
    }

    public PsiClassMock setContainingFile(PsiFile containingFile) {
        _containingFile = containingFile;

        return this;
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

    public PsiClassMock setValid(boolean valid) {
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
      return false;
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

    @Nullable
    public ASTNode getNode() {
        return null;
    }

  public boolean isEquivalentTo(PsiElement another) {
    return PsiClassImplUtil.isClassEquivalentTo(this, another);
  }

  public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    public <T> void putUserData(@NotNull Key<T> key, T value) {
    }

    public Icon getIcon(int flags) {
        return null;
    }

    @Nullable
    @NonNls
    public String getName() {
        return null;
    }

    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null;
    }

    @Nullable
    public PsiModifierList getModifierList() {
        return null;
    }

    public boolean hasModifierProperty(@NonNls @NotNull String name) {
        return false;
    }

    @Nullable
    public PsiDocComment getDocComment() {
        return null;
    }

    public boolean isDeprecated() {
        return false;
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
    public PsiMetaData getMetaData() {
        return null;
    }

    public boolean isMetaEnough() {
        return false;
    }

    public boolean hasTypeParameters() {
        return false;
    }

    @Nullable
    public PsiTypeParameterList getTypeParameterList() {
        return null;
    }

    @NotNull
    public PsiTypeParameter[] getTypeParameters() {
        return new PsiTypeParameter[0];
    }
}
