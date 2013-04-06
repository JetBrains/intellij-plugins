package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbOpenBlockMustacheImpl extends HbPsiElementImpl implements HbOpenBlockMustache {
    public HbOpenBlockMustacheImpl(@NotNull ASTNode astNode) {
        super(astNode);
    }
}
