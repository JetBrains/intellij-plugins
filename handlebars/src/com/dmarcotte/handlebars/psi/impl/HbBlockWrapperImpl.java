package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbBlockWrapper;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbBlockWrapperImpl extends HbPsiElementImpl implements HbBlockWrapper {
    public HbBlockWrapperImpl(@NotNull ASTNode astNode) {
        super(astNode);
    }
}
