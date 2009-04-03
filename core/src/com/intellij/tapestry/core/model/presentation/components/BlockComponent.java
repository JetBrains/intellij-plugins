package com.intellij.tapestry.core.model.presentation.components;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * The built-in Block element.
 */
public class BlockComponent extends Component {

    private static Map<String, TapestryParameter> _parameters;

    protected BlockComponent(IJavaClassType componentClass, TapestryProject project) throws NotTapestryElementException {
        super(componentClass, project);
    }

    /**
     * Returns an instance of the Block component.
     *
     * @param tapestryProject the current Tapestry project.
     * @return an instance of the Block component.
     */
    public synchronized static BlockComponent getInstance(TapestryProject tapestryProject) {
        if (_parameters == null) {
            _parameters = new HashMap<String, TapestryParameter>();

            _parameters.put("id", new DummyTapestryParameter(tapestryProject, "id", false));
        }

        return new BlockComponent(tapestryProject.getJavaTypeFinder().findType("org.apache.tapestry.internal.parser.BlockToken", true), tapestryProject);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "block";
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
        return "block";
    }
}
