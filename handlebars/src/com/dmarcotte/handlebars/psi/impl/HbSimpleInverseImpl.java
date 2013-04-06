package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbSimpleInverse;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbSimpleInverseImpl extends HbPsiElementImpl implements HbSimpleInverse {
    public HbSimpleInverseImpl(@NotNull ASTNode astNode) {
        super(astNode);
    }
}
