import {
  <symbolName descr="identifiers//exported function">Component</symbolName>,
  <symbolName descr="identifiers//exported function">Directive</symbolName>,
  <symbolName descr="identifiers//exported function">HostBinding</symbolName>,
  <symbolName descr="identifiers//exported function">HostListener</symbolName>,
  <symbolName descr="identifiers//exported function">Input</symbolName>,
} <info>from</info> '@angular/core';
import {<symbolName descr="classes//exported class">NgClass</symbolName>} <info>from</info> "@angular/common";

<info descr="decorator">@</info><info descr="decorator">Directive</info>({
   <symbolName descr="instance field">selector</symbolName>: '[appClicks]',
   <symbolName descr="instance field">standalone</symbolName>: true,
   <symbolName descr="instance field">host</symbolName>: {
     '<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[title]</symbolName>': '<symbolName descr="instance field"><inject>foo</inject></symbolName>',
     '<symbolName textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">(click)</symbolName>': '<inject><symbolName descr="instance method">onClick</symbolName>()</inject>'
   }
 })
export <info>abstract</info> class <symbolName descr="classes//exported class">AppClicksDirective</symbolName> {
  <symbolName descr="instance field">foo</symbolName>!: <info>string</info>
  <symbolName descr="instance method">onClick</symbolName>() {
    <symbolName descr="identifiers//global variable">console</symbolName>.<symbolName descr="instance method">log</symbolName>('Click');
  }
}

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   <symbolName descr="instance field">selector</symbolName>: '<symbolName textAttributesKey="HTML_TAG_NAME">oy-chip</symbolName>',
   <symbolName descr="instance field">template</symbolName>: `<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">
        <div [class.<symbolName textAttributesKey="CSS.CLASS_NAME">oy-chip--small</symbolName>]="<symbolName descr="instance field">small</symbolName>"></div>
        <div [ngClass]="{'<symbolName textAttributesKey="CSS.CLASS_NAME">oy-chip--small</symbolName>' : <symbolName descr="instance field">small</symbolName>}"></div>
        <div [ngClass]="['<symbolName textAttributesKey="CSS.CLASS_NAME">oy-chip--small</symbolName>', '<warning descr="Unrecognized name"><symbolName textAttributesKey="CSS.CLASS_NAME">small</symbolName></warning>']"></div>
    </inject>`,
   <symbolName descr="instance field">standalone</symbolName>: true,
   <symbolName descr="instance field">styles</symbolName>: `<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">
      .<info textAttributesKey="CSS.CLASS_NAME">oy-chip</info> {
        &.<info textAttributesKey="CSS.CLASS_NAME">oy-chip--small</info> {
        }
      }
   </inject>`,
   <symbolName descr="instance field">host</symbolName>: {
     "<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">class</symbolName>": "<symbolName textAttributesKey="CSS.CLASS_NAME">oy-chip oy-chip--small</symbolName>",
     "<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">title</symbolName>": "some",
     "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[class.<symbolName textAttributesKey="CSS.CLASS_NAME">oy-chip--small</symbolName>]</symbolName>": "<symbolName descr="instance field"><inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">small</inject></symbolName>",
     "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[attr.<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">accesskey</symbolName>]</symbolName>": "<symbolName descr="instance field"><inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">small</inject></symbolName>",
     "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[style.<symbolName textAttributesKey="CSS.PROPERTY_NAME">accent-color</symbolName>]</symbolName>": "<symbolName descr="instance field"><inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">small</inject></symbolName>",
     "<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[bar]</symbolName>": "<symbolName descr="instance field"><inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">small</inject></symbolName>",
     '<symbolName textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">(keydown)</symbolName>': '<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT"><symbolName descr="instance method">onKeyDown</symbolName>(<symbolName descr="identifiers//global variable">$event</symbolName>)</inject>',
   },
   <symbolName descr="instance field">imports</symbolName>: [
     <symbolName descr="classes//exported class">NgClass</symbolName>
   ]
 })
export class <symbolName descr="classes//exported class">ChipComponent</symbolName> {
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">class.<symbolName textAttributesKey="CSS.CLASS_NAME">oy-chip--small</symbolName></symbolName>")
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">attr.<symbolName textAttributesKey="HTML_ATTRIBUTE_NAME">title</symbolName></symbolName>")
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">style.<symbolName textAttributesKey="CSS.PROPERTY_NAME">align-content</symbolName></symbolName>")
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">bar</symbolName>")
  <info descr="decorator">@</info><info descr="decorator">Input</info>('<symbolName textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">small</symbolName>')
  public <symbolName descr="instance field">small</symbolName>: <info>boolean</info> = false;

  <info descr="decorator">@</info><info descr="decorator">HostListener</info>('<symbolName textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">keydown</symbolName>', ['$event'])
  <symbolName descr="instance method">onKeyDown</symbolName>(<warning descr="Unused parameter $event" textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><weak_warning descr="TS6133: '$event' is declared but its value is never read." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><symbolName descr="identifiers//parameter">$event</symbolName></weak_warning></warning>: <info>string</info>) {
  }
}
