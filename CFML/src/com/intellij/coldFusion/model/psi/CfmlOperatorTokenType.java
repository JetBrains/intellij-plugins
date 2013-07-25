package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.psi.tree.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 28.04.2009
 */
public class CfmlOperatorTokenType extends IElementType {

    private CfmlExpressionTypeCalculator myTypeCalculator;
    
    public CfmlOperatorTokenType(@org.jetbrains.annotations.NotNull String debugName,
                                 CfmlExpressionTypeCalculator typeCalculator) {
        super(debugName, CfmlLanguage.INSTANCE);
        myTypeCalculator = typeCalculator;
    }

    public CfmlExpressionTypeCalculator getTypeCalculator() {
        return myTypeCalculator;
    }
}
