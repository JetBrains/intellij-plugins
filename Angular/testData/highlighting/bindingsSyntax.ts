import {<symbolName descr="identifiers//exported function">booleanAttribute</symbolName>, <symbolName descr="identifiers//exported function">Component</symbolName>, <symbolName descr="identifiers//exported function">Input</symbolName>} <info descr="null">from</info> '@angular/core';

<info descr="decorator">@</info><info descr="decorator">Component</info>({
    <symbolName descr="instance field">inputs</symbolName>: [
        { <symbolName descr="instance field">name</symbolName>: "<symbolName descr="TS.INSTANCE_MEMBER_VARIABLE">obj</symbolName>" },
        { <symbolName descr="instance field">name</symbolName>: "<symbolName descr="TS.INSTANCE_MEMBER_VARIABLE">obj2</symbolName>", <symbolName descr="instance field">alias</symbolName>:"<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">foo-bar</symbolName>" },
        { <symbolName descr="instance field">name</symbolName>: "<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">obj_virtual</symbolName>" },
        "<symbolName descr="TS.INSTANCE_MEMBER_VARIABLE">obj</symbolName>",
        "<symbolName descr="TS.INSTANCE_MEMBER_VARIABLE">obj</symbolName>:<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">foobar</symbolName>"
    ],
    <symbolName descr="instance field">outputs</symbolName>: [
      "<symbolName descr="TS.INSTANCE_MEMBER_VARIABLE">obj</symbolName>",
      "<symbolName descr="TS.INSTANCE_MEMBER_VARIABLE">obj</symbolName>:<symbolName descr="NG.EVENT_BINDING_ATTR_NAME">foobar</symbolName>",
    ],
    <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">app-test</symbolName>',
    <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
        <app-test [foo-bar]="true"/>
        <app-test [obj_virtual]="true"/>
        <div [class.<symbolName descr="CSS.CLASS_NAME">foo</symbolName>]="true"></div>
        <div [attr.<symbolName descr="HTML_ATTRIBUTE_NAME">title</symbolName>]="true"></div>
        <div [style.<symbolName descr="CSS.PROPERTY_NAME">accelerator</symbolName>]="true"></div>
    </inject>`
})
export class <symbolName descr="classes//exported class">TestComponent</symbolName> {
    <info descr="decorator">@</info><info descr="decorator">Input</info>({<symbolName descr="instance field">alias</symbolName>: "<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">foo</symbolName>", <symbolName descr="instance field">transform</symbolName>: <symbolName descr="identifiers//exported function">booleanAttribute</symbolName>})
    <symbolName descr="instance field">nope</symbolName>!: <info descr="null">boolean</info>;

    <info descr="decorator">@</info><info descr="decorator">Input</info>("<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">bar</symbolName>")
    <symbolName descr="instance field">alsoNo</symbolName>!: <info descr="null">boolean</info>;

    <info descr="decorator">@</info><info descr="decorator">Input</info>()
    <symbolName descr="instance field">strict</symbolName>!: <info descr="null">boolean</info>;

    <symbolName descr="instance field">obj</symbolName>!: <info descr="null">boolean</info>;

    <symbolName descr="instance field">obj2</symbolName>!: <info descr="null">boolean</info>;

}
