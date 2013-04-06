package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbOpenInverseBlockMustache;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbOpenInverseBlockMustacheImpl extends HbOpenBlockMustacheImpl implements HbOpenInverseBlockMustache {
    public HbOpenInverseBlockMustacheImpl(@NotNull ASTNode astNode) {
        super(astNode);
    }
}
