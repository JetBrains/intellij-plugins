import {
  <symbolName descr="identifiers//exported function">Component</symbolName>,
  <symbolName descr="identifiers//exported function">Directive</symbolName>,
} <info>from</info> '@angular/core';

<info descr="decorator">@</info><info descr="decorator">Directive</info>({
  <symbolName descr="instance field">selector</symbolName>: '<symbolName textAttributesKey="HTML_TAG_NAME">div</symbolName>.oy-chip--small[<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">appClicks</symbolName>=value]',
  <symbolName descr="instance field">standalone</symbolName>: true,
})
export class <symbolName descr="classes//exported class">AppClicksDirective</symbolName> {
  <warning descr="Unused field foo" textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><symbolName descr="instance field">foo</symbolName></warning>!: <info>string</info>
  <warning descr="Unused method onClick" textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><symbolName descr="instance method">onClick</symbolName></warning>() {
    <symbolName descr="identifiers//global variable">console</symbolName>.<symbolName descr="instance method">log</symbolName>('Click');
  }
}

<info descr="decorator">@</info><info descr="decorator">Component</info>({
  <symbolName descr="instance field">selector</symbolName>: '<symbolName textAttributesKey="HTML_TAG_NAME">div</symbolName>.oy-chip--small[<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">appClicks</symbolName>=value]',
  <symbolName descr="instance field">template</symbolName>: `<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">
        <ng-content select="<symbolName textAttributesKey="HTML_TAG_NAME">div</symbolName>.oy-chip--small[<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">appClicks</symbolName>=value]"></ng-content>
    </inject>`,
  <symbolName descr="instance field">standalone</symbolName>: true,
  <symbolName descr="instance field">styles</symbolName>: `<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">
      .<info textAttributesKey="CSS.CLASS_NAME">oy-chip</info> {
        &.<info textAttributesKey="CSS.CLASS_NAME">oy-chip--small</info> {
        }
      }
   </inject>`,
})
export class <symbolName descr="classes//exported class">ChipComponent</symbolName> {
}
