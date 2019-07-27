package com.intellij.tapestry.core.model.externalizable.toclasschain;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import org.apache.commons.chain.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Externalizes a component to be included in a class.
 */
public class ComponentExternalizer extends ToClassExternalizer {

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getContext().getElement() instanceof TapestryComponent))
            return false;

        TapestryComponent component = (TapestryComponent) getContext().getElement();

        IJavaField field = component.getProject().getJavaTypeCreator().createField(
          StringUtil.notNullize(component.getElementClass().getName()), component.getElementClass(), true, true);

        String suggestedFieldName = suggestName(field.getName(), new ArrayList<>(getContext().getTargetClass().getFields(false).keySet()));
        if (!suggestedFieldName.equals(field.getName())) {
            field = component.getProject().getJavaTypeCreator().createField(suggestedFieldName, component.getElementClass(), true, true);
        }

        Map<String, String> annotationParameters = new HashMap<>();
        StringBuilder parametersParameter = new StringBuilder("{");
        for (TapestryParameter parameter : component.getParameters().values())
            if (parameter.isRequired()) {
                parametersParameter.append("\"").append(parameter.getName()).append("=\",");
            }

        if (parametersParameter.length() > 1) {
            parametersParameter.deleteCharAt(parametersParameter.length() - 1);
            parametersParameter.append("}");
            annotationParameters.put("parameters", parametersParameter.toString());
        }

        component.getProject().getJavaTypeCreator().createFieldAnnotation(field, TapestryConstants.COMPONENT_ANNOTATION, annotationParameters);

        String serialized = field.getStringRepresentation();
        if (component.getProject().getJavaTypeCreator().ensureClassImport(getContext().getTargetClass(), component.getElementClass())) {
            serialized = serialized.replace(component.getElementClass().getFullyQualifiedName(), component.getElementClass().getName());
        }

        if (component.getProject().getJavaTypeCreator().ensureClassImport(getContext().getTargetClass(), component.getProject().getJavaTypeFinder().findType(TapestryConstants.COMPONENT_ANNOTATION, true))) {
            serialized = serialized.replace(TapestryConstants.COMPONENT_ANNOTATION, "Component");
        }

        getContext().setResult("\n" + serialized + "\n");

        return true;
    }
}
