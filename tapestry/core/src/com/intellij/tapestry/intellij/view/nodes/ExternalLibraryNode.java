package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.model.Library;
import com.intellij.ui.treeStructure.SimpleNode;

import java.util.ArrayList;
import java.util.List;

public class ExternalLibraryNode extends TapestryNode {

    public ExternalLibraryNode(Library library, Module module, AbstractTreeBuilder treeBuilder) {
        super(module, treeBuilder);

        init(library, new PresentationData(library.getId(), library.getId(), AllIcons.Modules.Library, null));
    }

    /**
     * {@inheritDoc}
     */
    public SimpleNode[] getChildren() {
        List<TapestryNode> children = new ArrayList<>();

        Library library = ((Library) getElement());

        if (library.getPages().size() > 0) {
            children.add(
                    new PagesNode(
                            library,
                            JavaPsiFacade.getInstance(myProject).findPackage(library.getBasePackage() + "." + TapestryConstants.PAGES_PACKAGE)
                                    .getDirectories(GlobalSearchScope.moduleWithLibrariesScope(_module))[0], _module, _treeBuilder
                    )
            );
        }

        if (library.getComponents().size() > 0) {
            children.add(
                    new ComponentsNode(
                            library,
                            JavaPsiFacade.getInstance(myProject).findPackage(library.getBasePackage() + "." + TapestryConstants.COMPONENTS_PACKAGE)
                                    .getDirectories(GlobalSearchScope.moduleWithLibrariesScope(_module))[0], _module, _treeBuilder
                    )
            );
        }

        if (library.getMixins().size() > 0) {
            children.add(
                    new MixinsNode(
                            library,
                            JavaPsiFacade.getInstance(myProject).findPackage(library.getBasePackage() + "." + TapestryConstants.MIXINS_PACKAGE)
                                    .getDirectories(GlobalSearchScope.moduleWithLibrariesScope(_module))[0], _module, _treeBuilder
                    )
            );
        }

        return children.toArray(new TapestryNode[0]);
    }
}
