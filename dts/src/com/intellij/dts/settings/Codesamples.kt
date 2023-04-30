package com.intellij.dts.settings

object Codesamples {
    val coloring = """
/dts-v1/;

// comment
/memreserve/ 0x800 0x010;

/ {
    <PROPERTY>property</PROPERTY> = "string", "escaped<ESCAPED>\"</ESCAPED>", <42>;
    <PROPERTY>expression</PROPERTY> = <(8 * 4 % 5) (0 > 3 ? 8 : 9)>;

    <LABEL>label</LABEL>: <NODE_NAME>name</NODE_NAME>@<NODE_ADDR>addr</NODE_ADDR> {};
};

&ref {
    <PROPERTY>property</PROPERTY> = &{/path/ref};
}; 
"""
}