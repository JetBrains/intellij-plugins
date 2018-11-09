package com.intellij.tapestry.core.model.presentation.valueresolvers;

import com.intellij.tapestry.core.java.AssignableToAll;
import org.apache.commons.chain.Context;

/**
 * Resolves component values.
 */
public class ComponentResolver extends AbstractValueResolver {

    private static final String PREFIX = "component";

    @Override
    public boolean execute(Context context) throws Exception {
        String prefix = getPrefix(((ValueResolverContext) context).getValue(), ((ValueResolverContext) context).getDefaultPrefix());

        if (prefix == null || !prefix.equals(PREFIX))
            return false;

        ((ValueResolverContext) context).setResultType(AssignableToAll.getInstance());

        return true;
    }
}
