/*----------------------------------------------------------------
 *  Copyright (c) ThoughtWorks, Inc.
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/

package com.thoughtworks.gauge.language.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface ConceptArg extends PsiElement {

    @Nullable
    ConceptDynamicArg getDynamicArg();

    @Nullable
    ConceptStaticArg getStaticArg();

}
