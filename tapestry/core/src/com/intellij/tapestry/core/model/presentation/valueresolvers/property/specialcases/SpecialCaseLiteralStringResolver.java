package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

import java.util.regex.Pattern;

/**
 * Resolves the special case when a property value is given as a string literal.
 */
public class SpecialCaseLiteralStringResolver extends AbstractValueResolver {

    private static final Pattern PATTERN = Pattern.compile("'.*'");

    public boolean execute(Context context) throws Exception {
        String cleanValue = getCleanValue(((ValueResolverContext) context).getValue()).trim().toLowerCase();

        if (PATTERN.matcher(cleanValue).matches()) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType("java.lang.String", true));

            return true;
        }

        return false;
    }
}
