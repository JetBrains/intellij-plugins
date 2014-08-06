package com.intellij.tapestry.core.java.coercion;

import com.intellij.psi.CommonClassNames;
import com.intellij.tapestry.core.java.IJavaClassType;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaClassTypeCoercionValidator implements Command {

    /**
     * List of all possible coercions.
     * The key is the type to coerce to, the values are the types that can be coerced to the key type.
     */
    private static final Map<String, List<String>> CLASS_COERCION_MAP = new HashMap<String, List<String>>();

    static {
        CLASS_COERCION_MAP.put(CommonClassNames.JAVA_LANG_STRING, Arrays.asList(
          CommonClassNames.JAVA_LANG_OBJECT));

        CLASS_COERCION_MAP.put("java.lang.Double", Arrays.asList(
                CommonClassNames.JAVA_LANG_STRING,
                "java.math.BigDecimal",
                "java.lang.Long",
                "java.lang.Float"));

        CLASS_COERCION_MAP.put("java.math.BigDecimal", Arrays.asList(
                CommonClassNames.JAVA_LANG_STRING
        ));

        CLASS_COERCION_MAP.put("java.math.BigInteger", Arrays.asList(
                CommonClassNames.JAVA_LANG_STRING
        ));

        CLASS_COERCION_MAP.put("java.lang.Long", Arrays.asList(
                CommonClassNames.JAVA_LANG_STRING,
                "java.lang.Number",
                "org.apache.tapestry5.ioc.util.TimeInterval"
        ));

        CLASS_COERCION_MAP.put("java.lang.Byte", Arrays.asList(
                "java.lang.Long"
        ));

        CLASS_COERCION_MAP.put("java.lang.Short", Arrays.asList(
                "java.lang.Long"
        ));

        CLASS_COERCION_MAP.put("java.lang.Integer", Arrays.asList(
                "java.lang.Long"
        ));

        CLASS_COERCION_MAP.put("java.lang.Float", Arrays.asList(
                "java.lang.Double"
        ));

        CLASS_COERCION_MAP.put("java.lang.Boolean", Arrays.asList(
          CommonClassNames.JAVA_LANG_OBJECT
        ));

        CLASS_COERCION_MAP.put(CommonClassNames.JAVA_UTIL_LIST, Arrays.asList(
          CommonClassNames.JAVA_LANG_OBJECT
        ));

        CLASS_COERCION_MAP.put("org.apache.tapestry5.grid.GridDataSource", Arrays.asList(
          CommonClassNames.JAVA_UTIL_LIST
        ));

        CLASS_COERCION_MAP.put("org.apache.tapestry5.ioc.util.TimeInterval", Arrays.asList(
          CommonClassNames.JAVA_LANG_STRING
        ));

        CLASS_COERCION_MAP.put("java.text.DateFormat", Arrays.asList(
          CommonClassNames.JAVA_LANG_STRING
        ));
    }

    public boolean execute(Context context) throws Exception {
        if (!(((CoercionContext) context).getSourceType() instanceof IJavaClassType && ((CoercionContext) context).getTargetType() instanceof IJavaClassType))
            return false;

        List<String> coercions = CLASS_COERCION_MAP.get(((IJavaClassType) ((CoercionContext) context).getTargetType()).getFullyQualifiedName());

        if (coercions == null)
            return false;

        for (String typeName : coercions) {
            IJavaClassType type = ((CoercionContext) context).getProject().getJavaTypeFinder().findType(typeName, true);

            if (type != null && type.isAssignableFrom(((CoercionContext) context).getSourceType())) {
                ((CoercionContext) context).setResult(true);

                return true;
            }
        }

        return true;
    }
}
