package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

import java.util.regex.Pattern;

/**
 * Resolves the special case when a property value is given as a range of integers.
 */
public class SpecialCaseRangeIntegersResolver extends AbstractValueResolver {

    private static final Pattern PATTERN = Pattern.compile("\\d+\\.\\.\\d+");

    @Override
    public boolean execute(Context context) throws Exception {
        String cleanValue = StringUtil.toLowerCase(getCleanValue(((ValueResolverContext) context).getValue()).trim());

        if (PATTERN.matcher(cleanValue).matches()) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType("java.lang.Iterable", true));

            return true;
        }

        return false;
    }
}
