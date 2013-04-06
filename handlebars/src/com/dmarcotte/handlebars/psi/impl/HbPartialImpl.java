package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbPartial;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbPartialImpl extends HbPsiElementImpl implements HbPartial {
    public HbPartialImpl(@NotNull ASTNode astNode) {
        super(astNode);
    }
}
