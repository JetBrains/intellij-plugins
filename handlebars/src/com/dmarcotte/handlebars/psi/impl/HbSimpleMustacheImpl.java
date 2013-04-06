package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.psi.HbSimpleMustache;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class HbSimpleMustacheImpl extends HbMustacheImpl implements HbSimpleMustache {
    public HbSimpleMustacheImpl(@NotNull ASTNode astNode) {
        super(astNode);
    }
}
