import {<symbolName descr="identifiers//exported function">Component</symbolName>, <symbolName descr="identifiers//exported function">Directive</symbolName>, <symbolName descr="identifiers//exported function">EventEmitter</symbolName>, <symbolName descr="identifiers//exported function">Input</symbolName>, <symbolName descr="identifiers//exported function">Output</symbolName>} <info textAttributesKey="TS.KEYWORD">from</info> '@angular/core';

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Directive</symbolName>({
    <symbolName descr="instance field">selector</symbolName>: '[<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">appBold</symbolName>]',
    <symbolName descr="instance field">standalone</symbolName>: true,
    <symbolName descr="instance field">exportAs</symbolName>: "boldDir"
})
export class <symbolName descr="classes//exported class">BoldDirective</symbolName> {
    // EventEmitter interface has new() signature, so from TS point of view it's possible to write `new new EventEmitter()`, so we highlight `hover` as a method
    <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Output</symbolName>() <symbolName descr="instance method">hover</symbolName> = new <symbolName descr="identifiers//exported function">EventEmitter</symbolName>()
}

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Directive</symbolName>({
    <symbolName descr="instance field">selector</symbolName>: '[<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">appUnderline</symbolName>]',
    <symbolName descr="instance field">standalone</symbolName>: true
})
export class <symbolName descr="classes//exported class">UnderlineDirective</symbolName> {
    <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Input</symbolName>() <symbolName descr="instance field">color</symbolName> = 'black';
}

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Directive</symbolName>({
    <symbolName descr="instance field">selector</symbolName>: '[<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">appMouseenter</symbolName>]',
    <symbolName descr="instance field">standalone</symbolName>: true,
    <symbolName descr="instance field">exportAs</symbolName>: "boldDir,mouseDir",
    <symbolName descr="instance field">hostDirectives</symbolName>: [{
        <symbolName descr="instance field">directive</symbolName>: <symbolName descr="classes//exported class">BoldDirective</symbolName>,
        <symbolName descr="instance field">outputs</symbolName>: ['<symbolName textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">hover</symbolName>']
    }, {
        <symbolName descr="instance field">directive</symbolName>: <symbolName descr="classes//exported class">UnderlineDirective</symbolName>,
        <symbolName descr="instance field">inputs</symbolName>: ['<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">color</symbolName>: <symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">underlineColor</symbolName>']
    }]
})
export class <symbolName descr="classes//exported class">MouseenterDirective</symbolName> {

}

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Component</symbolName>({
    <symbolName descr="instance field">standalone</symbolName>: true,
    <symbolName descr="instance field">selector</symbolName>: '<symbolName textAttributesKey="HTML_TAG_NAME">resolved</symbolName>',
    <symbolName descr="instance field">template</symbolName>: "",
    /* priority goes to the host directive exportAs*/
    <symbolName descr="instance field">exportAs</symbolName>: "boldDir,fooDir",
    <symbolName descr="instance field">hostDirectives</symbolName>: [<symbolName descr="classes//exported class">MouseenterDirective</symbolName>]
})
export class <symbolName descr="classes//exported class">ResolvedComponent</symbolName> {

}

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Component</symbolName>({
    <symbolName descr="instance field">standalone</symbolName>: true,
    <symbolName descr="instance field">selector</symbolName>: '<symbolName textAttributesKey="HTML_TAG_NAME">app-test</symbolName>',
    <symbolName descr="instance field">template</symbolName>: '',
    <symbolName descr="instance field">imports</symbolName>: [
        <error descr="Component ResolvedComponent is never used in a component template"><symbolName descr="classes//exported class">ResolvedComponent</symbolName></error>,
        <error descr="Directive BoldDirective is never used in a component template"><symbolName descr="classes//exported class">BoldDirective</symbolName></error>,
        <error descr="Directive UnderlineDirective is never used in a component template"><symbolName descr="classes//exported class">UnderlineDirective</symbolName></error>
    ]
})
export class <symbolName descr="classes//exported class">TestComponent</symbolName> {

}
