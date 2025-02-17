package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.plugins.jade.psi.JadeStubBasedPsiElementBase;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import com.jetbrains.plugins.jade.psi.stubs.impl.JadeMixinDeclarationStubImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JadeMixinDeclarationImpl extends JadeStubBasedPsiElementBase<JadeMixinDeclarationStubImpl> implements PsiNameIdentifierOwner {


  public JadeMixinDeclarationImpl(@NotNull JadeMixinDeclarationStubImpl stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public JadeMixinDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull String getName() {
    JadeMixinDeclarationStubImpl stub = getStub();
    if (stub != null) {
      return StringUtil.notNullize(stub.getName());
    }

    PsiElement nameIdentifier = getNameIdentifier();
    if (nameIdentifier == null) {
      return "";
    }
    return nameIdentifier.getText();
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PsiElement identifier = getNameIdentifier();
    if (!(identifier instanceof LeafPsiElement)) {
      return null;
    }
    ((LeafPsiElement)identifier).replaceWithText(name);

    return this;
  }

  @Override
  public int getTextOffset() {
    PsiElement nameIdentifier = getNameIdentifier();
    if (nameIdentifier != null) {
      return nameIdentifier.getTextOffset();
    }
    else {
      return super.getTextOffset();
    }
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    return findChildByType(JadeTokenTypes.IDENTIFIER);
  }
}
