package com.intellij.dts.settings

object Codesamples {
    const val coloring = """
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

    const val indenting = """
/dts-v1/;

/ {
    property = "value";

    node {
        property = "value",
        "continuation";

        subNode {
            property = "value";
        };
    };
};
"""

    const val spacing = """
/dts-v1/;

/ {
    list = "value1", "value2";
    cells = <0x1 0x2 0x3>;
    bytes = [010203];
    empty = <>, [];
    expr = <((1 + 2) * 3 << 4 & 5) (0 || 1 ? 2 : 3)>;

    label: node { };
};
"""

    const val wrapping = """
/dts-v1/;

/ {
    property = "value";

    list = "value1",
    "value2";
    
    array = <0x00000000
    0x00000000>;
};
"""
}