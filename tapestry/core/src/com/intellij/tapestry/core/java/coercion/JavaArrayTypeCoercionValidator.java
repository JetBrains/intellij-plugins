package com.intellij.tapestry.core.java.coercion;

import com.intellij.psi.CommonClassNames;
import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaType;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

public class JavaArrayTypeCoercionValidator implements Command {

    @Override
    public boolean execute(Context context) throws Exception {

        // if none of the types are an array don't treat this case
        CoercionContext coercionContext = (CoercionContext)context;
        if (!(coercionContext.getSourceType() instanceof IJavaArrayType)) {
            if(coercionContext.getTargetType() instanceof IJavaArrayType) {
                IJavaType componentType = ((IJavaArrayType)coercionContext.getTargetType()).getComponentType();
                IJavaClassType objectType = coercionContext.getProject().getJavaTypeFinder().findType(
                  CommonClassNames.JAVA_LANG_OBJECT, true);

                if (componentType != null && componentType.isAssignableFrom(objectType)) {
                  coercionContext.setResult(true);
                  return true;
                }
            }
            return false;
        }

        // if the target type is a subtype os java.util.List then coerce
        if (coercionContext.getTargetType().isAssignableFrom(coercionContext.getProject().getJavaTypeFinder().findType(
          CommonClassNames.JAVA_UTIL_LIST, true))) {
            coercionContext.setResult(true);

            return true;
        }

        // if the target type is a boolean then coerce
        if (coercionContext.getTargetType().isAssignableFrom(coercionContext.getProject().getJavaTypeFinder().findType("java.lang.Boolean", true))) {
            coercionContext.setResult(true);

            return true;
        }

        // if the target type is a GridDataSource then coerce
        if ((coercionContext.getTargetType() instanceof IJavaClassType && ((IJavaClassType) (coercionContext.getTargetType())).getFullyQualifiedName().equals("org.apache.tapestry5.grid.GridDataSource"))) {
            coercionContext.setResult(true);

            return true;
        }

        return true;
    }
}
