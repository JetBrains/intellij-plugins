package com.intellij.tapestry.intellij.view.nodes;

import java.io.Serializable;
import java.util.Comparator;

public class PackageNodesComparator implements Comparator<TapestryNode>, Serializable {

    private static final Comparator<TapestryNode> ME = new PackageNodesComparator();
    private static final long serialVersionUID = -7641313690902495059L;

    public static Comparator<TapestryNode> getInstance() {
        return ME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(TapestryNode o1, TapestryNode o2) {
        if (o1 instanceof PackageNode && o2 instanceof PackageNode) {
            return o1.getPresentableText().compareTo(o2.getPresentableText());
        }

        if (o1 instanceof PackageNode && !(o2 instanceof PackageNode)) {
            return -1;
        }

        if (o2 instanceof PackageNode && !(o1 instanceof PackageNode)) {
            return 1;
        }

        if (!(o1 instanceof PackageNode) && !(o2 instanceof PackageNode)) {
            return o1.getPresentableText().compareTo(o2.getPresentableText());
        }

        return 0;
    }
}
