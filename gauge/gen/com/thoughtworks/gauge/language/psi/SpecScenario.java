// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpecScenario extends PsiElement {

    @NotNull
    List<SpecStep> getStepList();

    @Nullable
    SpecTags getTags();

}
