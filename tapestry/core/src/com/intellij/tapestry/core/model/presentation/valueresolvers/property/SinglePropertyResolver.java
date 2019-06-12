package com.intellij.tapestry.core.model.presentation.valueresolvers.property;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.util.ClassUtils;
import org.apache.commons.chain.Context;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Resolves the special case when a property value is given as a property name.
 */
public class SinglePropertyResolver extends AbstractValueResolver {

    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z$_][a-zA-Z0-9$_.]*");

    @Override
    public boolean execute(Context context) throws Exception {
        String cleanValue = getCleanValue(((ValueResolverContext) context).getValue());

        if (cleanValue != null && PATTERN.matcher(cleanValue).matches()) {
            Map<String, Object> properties = ClassUtils.getClassProperties(((ValueResolverContext) context).getContextClass());

            for (Map.Entry<String, Object> property : properties.entrySet()) {
                if (StringUtil.toLowerCase(property.getKey()).equals(StringUtil.toLowerCase(cleanValue))) {

                    if (property.getValue() instanceof IJavaMethod)
                        ((ValueResolverContext) context).setResultType(((IJavaMethod) property.getValue()).getReturnType());
                    else if (property.getValue() instanceof IJavaField)
                        ((ValueResolverContext) context).setResultType(((IJavaField) property.getValue()).getType());
                    else
                        continue;

                    ((ValueResolverContext) context).setResultCodeBind(property.getValue());

                    return true;
                }
            }

            return true;
        }

        return false;
    }
}
