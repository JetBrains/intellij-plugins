package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbCloseBlockMustache;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbCloseBlockMustacheImpl extends HbPsiElementImpl implements HbCloseBlockMustache {
    public HbCloseBlockMustacheImpl(@NotNull ASTNode astNode) {
        super(astNode);
    }
}
