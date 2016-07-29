package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.psi.HbOpenPartialBlockMustache;
import com.dmarcotte.handlebars.psi.HbPartial;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * Created by adi.d on 7/20/16.
 */
public class HbPsiReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        if (HbConfig.isResolvePartialsPathsFromNameEnabled()) {
            HbPsiReferenceProvider provider = new HbPsiReferenceProvider();

            psiReferenceRegistrar.registerReferenceProvider(StandardPatterns.instanceOf(HbPartial.class), provider);
            psiReferenceRegistrar.registerReferenceProvider(StandardPatterns.instanceOf(HbOpenPartialBlockMustache.class), provider);
        }
    }
}
