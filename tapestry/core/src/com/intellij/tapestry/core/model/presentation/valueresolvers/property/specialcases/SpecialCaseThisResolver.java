package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

/**
 * Resolves the special case when a property value is given as a this literal.
 */
public class SpecialCaseThisResolver extends AbstractValueResolver {

    @Override
    public boolean execute(Context context) throws Exception {
        String cleanValue = getCleanValue(((ValueResolverContext) context).getValue()).trim().toLowerCase();

        if (cleanValue.equals("this")) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getContextClass());

            return true;
        }

        return false;
    }
}
