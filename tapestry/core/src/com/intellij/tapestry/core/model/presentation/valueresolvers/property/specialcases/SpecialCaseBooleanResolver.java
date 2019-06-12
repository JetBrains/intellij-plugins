package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

/**
 * Resolves the special case when a property value is given as a boolean literal.
 */
public class SpecialCaseBooleanResolver extends AbstractValueResolver {

    @Override
    public boolean execute(Context context) throws Exception {
        String cleanValue = StringUtil.toLowerCase(getCleanValue(((ValueResolverContext) context).getValue()).trim());

        if (cleanValue.equals("true") || cleanValue.equals("false")) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType("java.lang.Boolean", true));

            return true;
        }

        return false;
    }
}
