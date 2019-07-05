package com.intellij.tapestry.core.model.externalizable.totemplatechain;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.util.PathUtils;
import org.apache.commons.chain.Context;

import java.util.Locale;

/**
 * Externalizes a component to be included in a template.
 */
public class ComponentExternalizer extends ToTemplateExternalizer {

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getContext().getElement() instanceof TapestryComponent))
            return false;

        TapestryComponent component = (TapestryComponent) getContext().getElement();

        String componentName;
        if (component.getLibrary().getId().equals(TapestryProject.APPLICATION_LIBRARY_ID) || component.getLibrary().getId().equals(TapestryProject.CORE_LIBRARY_ID)) {
            componentName = PathUtils.pathIntoPackage(component.getName().toLowerCase(Locale.getDefault()), false);
        } else {
            componentName = component.getLibrary().getId() + "." + PathUtils.pathIntoPackage(component.getName().toLowerCase(Locale.getDefault()), false);
        }

        StringBuilder serialized = new StringBuilder();

        serialized.append("<").append(getContext().getNamespacePrefix()).append(":").append(componentName);
        for (TapestryParameter parameter : component.getParameters().values())
            if (parameter.isRequired()) {
                serialized.append(" ").append(parameter.getName()).append("=\"\"");
            }

        serialized.append("></").append(getContext().getNamespacePrefix()).append(":").append(componentName).append(">");

        getContext().setResult(serialized.toString());

        return true;
    }
}
