package com.intellij.tapestry.core.model.presentation.components;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * The built-in Container element.
 */
public class ContainerComponent extends Component {
    private static Map<String, TapestryParameter> _parameters = new HashMap<String, TapestryParameter>();

    protected ContainerComponent(IJavaClassType componentClass, TapestryProject project) throws NotTapestryElementException {
        super(componentClass, project);
    }

    /**
     * Returns an instance of the Body component.
     *
     * @param tapestryProject the current Tapestry project.
     * @return an instance of the Body component.
     */
    public static ContainerComponent getInstance(TapestryProject tapestryProject) {
        return new ContainerComponent(tapestryProject.getJavaTypeFinder().findType("org.apache.tapestry.internal.parser.TemplateToken", true), tapestryProject);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "container";
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, TapestryParameter> getParameters() {
        return _parameters;
    }

    /**
     * {@inheritDoc}
     */
    protected String getElementNameFromClass(String rootPackage) throws NotTapestryElementException {
        return "container";
    }
}
