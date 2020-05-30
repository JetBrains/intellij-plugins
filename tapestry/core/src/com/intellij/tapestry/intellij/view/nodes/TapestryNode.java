package com.intellij.tapestry.intellij.view.nodes;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.module.Module;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all Tapestry related nodes.
 */
public abstract class TapestryNode extends SimpleNode {

    protected final AbstractTreeBuilder _treeBuilder;
    protected Module _module;
    protected Object _element;
    protected ItemPresentation _presentation;

    public TapestryNode(@NotNull final Module module, @NotNull final AbstractTreeBuilder treeBuilder) {
        super(module.getProject());

        _module = module;
        _treeBuilder = treeBuilder;
    }

    /**
     * Initializes the node.
     *
     * @param id           the node id.
     * @param presentation the node presentation configuration.
     */
    public void init(@NotNull final Object id, @NotNull final ItemPresentation presentation) {

        _element = id;
        _presentation = presentation;
        setIcon(_presentation.getIcon(false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleNode @NotNull [] getChildren() {
        return new SimpleNode[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getElement() {
        return _element;
    }

    public Module getModule() {
        return _module;
    }

    public String getPresentableText() {
        return _presentation.getPresentableText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doUpdate() {
        _presentation = updatePresentation(_presentation);
        setIcon(_presentation.getIcon(false));
        setPlainText(_presentation.getPresentableText());
    }

    /**
     * You can override this method and change presentation data.
     *
     * @param presentation Presentation data for update.
     * @return updated presentation
     */
    protected ItemPresentation updatePresentation(final ItemPresentation presentation) {
        return presentation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean expandOnDoubleClick() {
        return false;
    }
}
