package com.intellij.dts.lang.stubs;

import com.intellij.dts.lang.stubs.impl.DtsSubNodeStub;
import com.intellij.dts.lang.stubs.impl.DtsRefNodeStub;
import com.intellij.dts.lang.stubs.impl.DtsRootNodeStub;
import com.intellij.psi.tree.IElementType;

public interface DtsStubElementTypes {
    DtsRootNodeStub.Type ROOT_NODE = new DtsRootNodeStub.Type("ROOT_NODE");
    DtsRefNodeStub.Type REF_NODE = new DtsRefNodeStub.Type("REF_NODE");
    DtsSubNodeStub.Type SUB_NODE = new DtsSubNodeStub.Type("SUB_NODE");

    static IElementType factory(String name) {
        switch (name) {
            case "ROOT_NODE" -> { return ROOT_NODE; }
            case "REF_NODE" -> { return REF_NODE; }
            case "SUB_NODE" -> { return SUB_NODE; }
            default -> throw new IllegalArgumentException("Unknown name");
        }
    }
}

