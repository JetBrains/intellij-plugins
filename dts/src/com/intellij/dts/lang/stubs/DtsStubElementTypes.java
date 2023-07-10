package com.intellij.dts.lang.stubs;

import com.intellij.psi.tree.IElementType;

public interface DtsStubElementTypes {
    DtsRootNodeStub.Type ROOT_NODE = new DtsRootNodeStub.Type("ROOT_NODE");
    DtsSubNodeStub.Type SUB_NODE = new DtsSubNodeStub.Type("SUB_NODE");

    static IElementType factory(String name) {
        switch (name) {
            case "ROOT_NODE" -> { return ROOT_NODE; }
            case "SUB_NODE" -> { return SUB_NODE; }
            default -> throw new IllegalArgumentException("Unknown name");
        }
    }
}

