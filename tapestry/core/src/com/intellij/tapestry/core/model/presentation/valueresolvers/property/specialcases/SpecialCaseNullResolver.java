package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.CommonClassNames;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

/**
 * Resolves the special case when a property value is given as a null literal.
 */
public class SpecialCaseNullResolver extends AbstractValueResolver {

    @Override
    public boolean execute(Context context) throws Exception {
        String cleanValue = StringUtil.toLowerCase(getCleanValue(((ValueResolverContext) context).getValue()).trim());

        if (cleanValue.equals("null")) {
            ((ValueResolverContext) context).setResultType(((ValueResolverContext) context).getProject().getJavaTypeFinder().findType(
              CommonClassNames.JAVA_LANG_OBJECT, true));

            return true;
        }

        return false;
    }
}
