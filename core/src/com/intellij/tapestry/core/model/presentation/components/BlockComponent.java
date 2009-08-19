package com.intellij.tapestry.core.model.presentation.components;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The built-in Block element.
 */
public class BlockComponent extends Component {

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
        if (tapestryProject.getParameters().isEmpty()) {
            tapestryProject.getParameters().put("id", new DummyTapestryParameter(tapestryProject, "id", false));
        }

      final IJavaClassType classType =
          tapestryProject.getJavaTypeFinder().findType("org.apache.tapestry5.internal.parser.BlockToken", true);
      return classType == null ? null : new BlockComponent(classType, tapestryProject);
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
    @NotNull
    public Map<String, TapestryParameter> getParameters() {
        return getProject().getParameters();
    }

    /**
     * {@inheritDoc}
     */
    protected String getElementNameFromClass(String rootPackage) throws NotTapestryElementException {
        return "block";
    }
}
