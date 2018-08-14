package com.intellij.tapestry.core.model.presentation.valueresolvers;

import org.apache.commons.chain.Context;

/**
 * Resolves message values.
 */
public class MessageResolver extends AbstractValueResolver {

    private static final String PREFIX = "message";

    @Override
    public boolean execute(Context context) throws Exception {
        String prefix = getPrefix(((ValueResolverContext) context).getValue(), ((ValueResolverContext) context).getDefaultPrefix());

        if (prefix == null || !prefix.equals(PREFIX))
            return false;

        ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType("java.lang.String", true));

        return true;
    }
}
