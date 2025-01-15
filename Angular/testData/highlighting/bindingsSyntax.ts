import {<symbolName descr="identifiers//exported function">booleanAttribute</symbolName>, <symbolName descr="identifiers//exported function">Component</symbolName>, <symbolName descr="identifiers//exported function">Input</symbolName>} <info textAttributesKey="TS.KEYWORD">from</info> '@angular/core';

<info descr="decorator">@</info><info descr="decorator">Component</info>({
    <symbolName descr="instance field">inputs</symbolName>: [
        { <symbolName descr="instance field">name</symbolName>: "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">obj</symbolName>" },
        { <symbolName descr="instance field">name</symbolName>: "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">obj2</symbolName>", <symbolName descr="instance field">alias</symbolName>:"<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">foo-bar</symbolName>" },
        { <symbolName descr="instance field">name</symbolName>: "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">obj_virtual</symbolName>" },
        "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">obj</symbolName>",
        "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">obj:foobar</symbolName>"
    ],
    <symbolName descr="instance field">outputs</symbolName>: [
      "<symbolName textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">obj</symbolName>",
      "<symbolName textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">obj:foobar</symbolName>",
    ],
    <symbolName descr="instance field">selector</symbolName>: '<symbolName textAttributesKey="HTML_TAG_NAME">app-test</symbolName>',
    <symbolName descr="instance field">template</symbolName>: `<inject>
        <app-test [foo-bar]="true"/>
        <app-test [obj_virtual]="true"/>
        <div [class.<symbolName textAttributesKey="CSS.CLASS_NAME">foo</symbolName>]="true"></div>
        <div [attr.<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">title</symbolName>]="true"></div>
        <div [style.<symbolName textAttributesKey="CSS.PROPERTY_NAME">accelerator</symbolName>]="true"></div>
    </inject>`
})
export class <symbolName descr="classes//exported class">TestComponent</symbolName> {
    <info descr="decorator">@</info><info descr="decorator">Input</info>({<symbolName descr="instance field">alias</symbolName>: "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">foo</symbolName>", <symbolName descr="instance field">transform</symbolName>: <symbolName descr="identifiers//exported function">booleanAttribute</symbolName>})
    <symbolName descr="instance field">nope</symbolName>!: <info textAttributesKey="TS.PRIMITIVE.TYPES">boolean</info>;

    <info descr="decorator">@</info><info descr="decorator">Input</info>("<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">bar</symbolName>")
    <symbolName descr="instance field">alsoNo</symbolName>!: <info textAttributesKey="TS.PRIMITIVE.TYPES">boolean</info>;

    <info descr="decorator">@</info><info descr="decorator">Input</info>()
    <symbolName descr="instance field">strict</symbolName>!: <info textAttributesKey="TS.PRIMITIVE.TYPES">boolean</info>;

    <symbolName descr="instance field">obj</symbolName>!: <info textAttributesKey="TS.PRIMITIVE.TYPES">boolean</info>;

    <symbolName descr="instance field">obj2</symbolName>!: <info textAttributesKey="TS.PRIMITIVE.TYPES">boolean</info>;

}
