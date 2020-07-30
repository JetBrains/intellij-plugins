/*----------------------------------------------------------------
 *  Copyright (c) ThoughtWorks, Inc.
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/

package com.thoughtworks.gauge.language.psi;

import com.thoughtworks.gauge.StepValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ConceptStep extends ConceptNamedElement {

    @NotNull
    List<ConceptArg> getArgList();

    @Nullable
    ConceptTable getTable();

    StepValue getStepValue();

}
