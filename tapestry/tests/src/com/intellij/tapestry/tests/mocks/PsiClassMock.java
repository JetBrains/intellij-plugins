package com.intellij.tapestry.tests.mocks;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
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

    @Override
    @Nullable
    @NonNls
    public String getQualifiedName() {
        return null;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isAnnotationType() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    @Nullable
    public PsiReferenceList getExtendsList() {
        return null;
    }

    @Override
    @Nullable
    public PsiReferenceList getImplementsList() {
        return null;
    }

    @Override
    public PsiClassType @NotNull [] getExtendsListTypes() {
        return PsiClassType.EMPTY_ARRAY;
    }

    @Override
    public PsiClassType @NotNull [] getImplementsListTypes() {
        return PsiClassType.EMPTY_ARRAY;
    }

    @Override
    @Nullable
    public PsiClass getSuperClass() {
        return null;
    }

    @Override
    public PsiClass @NotNull [] getInterfaces() {
        return PsiClass.EMPTY_ARRAY;
    }

    @Override
    public PsiClass @NotNull [] getSupers() {
        return PsiClass.EMPTY_ARRAY;
    }

    @Override
    public PsiClassType @NotNull [] getSuperTypes() {
        return PsiClassType.EMPTY_ARRAY;
    }

    @Override
    public PsiField @NotNull [] getFields() {
        return PsiField.EMPTY_ARRAY;
    }

    @Override
    public PsiMethod @NotNull [] getMethods() {
        return PsiMethod.EMPTY_ARRAY;
    }

    @Override
    public PsiMethod @NotNull [] getConstructors() {
        return PsiMethod.EMPTY_ARRAY;
    }

    @Override
    public PsiClass @NotNull [] getInnerClasses() {
        return PsiClass.EMPTY_ARRAY;
    }

    @Override
    public PsiClassInitializer @NotNull [] getInitializers() {
        return PsiClassInitializer.EMPTY_ARRAY;
    }

    @Override
    public PsiField @NotNull [] getAllFields() {
        return PsiField.EMPTY_ARRAY;
    }

    @Override
    public PsiMethod @NotNull [] getAllMethods() {
        return PsiMethod.EMPTY_ARRAY;
    }

    @Override
    public PsiClass @NotNull [] getAllInnerClasses() {
        return PsiClass.EMPTY_ARRAY;
    }

    @Override
    @Nullable
    public PsiField findFieldByName(String name, boolean checkBases) {
        return null;
    }

    @Override
    @Nullable
    public PsiMethod findMethodBySignature(@NotNull PsiMethod patternMethod, boolean checkBases) {
        return null;
    }

    @Override
    public PsiMethod @NotNull [] findMethodsBySignature(@NotNull PsiMethod patternMethod, boolean checkBases) {
        return PsiMethod.EMPTY_ARRAY;
    }

    @Override
    public PsiMethod @NotNull [] findMethodsByName(@NonNls String name, boolean checkBases) {
        return PsiMethod.EMPTY_ARRAY;
    }

    @Override
    @NotNull
    public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(@NotNull String name, boolean checkBases) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable
    public PsiClass findInnerClassByName(String name, boolean checkBases) {
        return null;
    }

    @Override
    @Nullable
    public PsiJavaToken getLBrace() {
        return null;
    }

    @Override
    @Nullable
    public PsiJavaToken getRBrace() {
        return null;
    }

    @Override
    @Nullable
    public PsiIdentifier getNameIdentifier() {
        return null;
    }

    @Override
    public PsiElement getScope() {
        return null;
    }

    @Override
    public boolean isInheritor(@NotNull PsiClass baseClass, boolean checkDeep) {
        return false;
    }

    @Override
    public boolean isInheritorDeep(@NotNull PsiClass baseClass, @Nullable PsiClass classToByPass) {
        return false;
    }

    //@Nullable
    //public PomMemberOwner getPom() {
    //    return null;
    //}
    //
    @Override
    @Nullable
    public PsiClass getContainingClass() {
        return null;
    }

    @Override
    @NotNull
    public Collection<HierarchicalMethodSignature> getVisibleSignatures() {
        throw new UnsupportedOperationException();
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
    public PsiElement getParent() {
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
        return _containingFile;
    }

    public PsiClassMock setContainingFile(PsiFile containingFile) {
        _containingFile = containingFile;

        return this;
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

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        throw new UnsupportedOperationException();
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
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor)
      throws IncorrectOperationException {
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

    public PsiClassMock setValid(boolean valid) {
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
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state,
                                       @Nullable PsiElement lastParent,
                                       @NotNull PsiElement place) {
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
    @Nullable
    public ASTNode getNode() {
        return null;
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return PsiClassImplUtil.isClassEquivalentTo(this, another);
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
    @NonNls
    public String getName() {
        return null;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null;
    }

    @Override
    @Nullable
    public PsiModifierList getModifierList() {
        return null;
    }

    @Override
    public boolean hasModifierProperty(@NonNls @NotNull String name) {
        return false;
    }

    @Override
    @Nullable
    public PsiDocComment getDocComment() {
        return null;
    }

    @Override
    public boolean isDeprecated() {
        return false;
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

    @Nullable
    public PsiMetaData getMetaData() {
        return null;
    }

    public boolean isMetaEnough() {
        return false;
    }

    @Override
    public boolean hasTypeParameters() {
        return false;
    }

    @Override
    @Nullable
    public PsiTypeParameterList getTypeParameterList() {
        return null;
    }

    @Override
    public PsiTypeParameter @NotNull [] getTypeParameters() {
        return PsiTypeParameter.EMPTY_ARRAY;
    }
}
