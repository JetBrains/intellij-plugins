package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

import java.util.regex.Pattern;

/**
 * Resolves the special case when a property value is given as a numeric literal.
 */
public class SpecialCaseNumericResolver extends AbstractValueResolver {

    private static final Pattern LONG_PATTERN = Pattern.compile("\\d+");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("\\d+((\\.|,)\\d+)");

    @Override
    public boolean execute(Context context) throws Exception {
        String cleanValue = StringUtil.toLowerCase(getCleanValue(((ValueResolverContext) context).getValue()).trim());

        if (LONG_PATTERN.matcher(cleanValue).matches()) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType("java.lang.Long", true));

            return true;
        }

        if (DOUBLE_PATTERN.matcher(cleanValue).matches()) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType("java.lang.Double", true));

            return true;
        }

        return false;
    }
}
