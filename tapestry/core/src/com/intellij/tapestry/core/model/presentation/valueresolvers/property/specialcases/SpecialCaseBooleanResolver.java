package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

/**
 * Resolves the special case when a property value is given as a boolean literal.
 */
public class SpecialCaseBooleanResolver extends AbstractValueResolver {

    public boolean execute(Context context) throws Exception {
        String cleanValue = getCleanValue(((ValueResolverContext) context).getValue()).trim().toLowerCase();

        if (cleanValue.equals("true") || cleanValue.equals("false")) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType("java.lang.Boolean", true));

            return true;
        }

        return false;
    }
}
