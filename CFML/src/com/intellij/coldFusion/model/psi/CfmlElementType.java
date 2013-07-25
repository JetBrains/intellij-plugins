package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.psi.tree.IElementType;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
public class CfmlElementType extends IElementType {
    public CfmlElementType(final String s) {
        super(s, CfmlLanguage.INSTANCE);
    }
}
