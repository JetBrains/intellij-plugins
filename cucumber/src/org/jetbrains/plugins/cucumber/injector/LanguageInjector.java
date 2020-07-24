// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.injector;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinPystring;

import java.util.Collections;
import java.util.List;

public class LanguageInjector implements MultiHostInjector {
    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(GherkinPystring.class);
    }

    @Override
    public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement context) {
        if (!(context instanceof GherkinPystring)) {
            return;
        }

        final PsiLanguageInjectionHost host = (PsiLanguageInjectionHost) context;
        final InjectedLanguage language = getLanguageFromHost(host);

        if (language != null) {
            final TextRange range = language.getRangeInsideHost(host);

            if (!range.isEmpty()) {
                registrar.startInjecting(language.getLanguage());
                registrar.addPlace(null, null, host, range);
                registrar.doneInjecting();
            }
        }
    }

    private static InjectedLanguage getLanguageFromHost(@NotNull final PsiLanguageInjectionHost host) {
        final String text = host.getFirstChild().getNextSibling().getText();

        return InjectedLanguage.getInjectedLanguage(text);
    }
}
