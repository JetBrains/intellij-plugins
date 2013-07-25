package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;

public class CfmlArgumentList extends CfmlCompositeElement {
    public CfmlArgumentList(ASTNode node) {
        super(node);
    }

    public CfmlExpression[] getArguments() {
        return findChildrenByClass(CfmlExpression.class);
    }
}
