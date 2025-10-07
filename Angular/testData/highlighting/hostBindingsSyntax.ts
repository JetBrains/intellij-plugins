import {
  <symbolName descr="identifiers//exported function">Component</symbolName>,
  <symbolName descr="identifiers//exported function">Directive</symbolName>,
  <symbolName descr="identifiers//exported function">HostBinding</symbolName>,
  <symbolName descr="identifiers//exported function">HostListener</symbolName>,
  <symbolName descr="identifiers//exported function">Input</symbolName>,
} <info descr="null">from</info> '@angular/core';
import {<symbolName descr="classes//exported class">NgClass</symbolName>} <info descr="null">from</info> "@angular/common";

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Directive</symbolName>({
   <symbolName descr="instance field">selector</symbolName>: '[appClicks]',
   <symbolName descr="instance field">standalone</symbolName>: true,
   <symbolName descr="instance field">host</symbolName>: {
     '<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">[<symbolName descr="TS.INSTANCE_MEMBER_VARIABLE">title</symbolName>]</symbolName>': '<symbolName descr="instance field"><inject descr="null">foo</inject></symbolName>',
     '<symbolName descr="NG.EVENT_BINDING_ATTR_NAME">(click)</symbolName>': '<inject descr="null"><symbolName descr="instance method">onClick</symbolName>()</inject>'
   }
 })
export <info descr="null">abstract</info> class <symbolName descr="classes//exported class">AppClicksDirective</symbolName> {
  <symbolName descr="instance field">foo</symbolName>!: <info descr="null">string</info>
  <symbolName descr="instance method">onClick</symbolName>() {
    <symbolName descr="identifiers//global variable">console</symbolName>.<symbolName descr="instance method">log</symbolName>('Click');
  }
}

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Component</symbolName>({
   <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">oy-chip</symbolName>',
   <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
        <div [class.<symbolName descr="CSS.CLASS_NAME">oy-chip--small</symbolName>]="<symbolName descr="instance field">small</symbolName>"></div>
        <div [ngClass]="{'<symbolName descr="CSS.CLASS_NAME">oy-chip--small</symbolName>' : <symbolName descr="instance field">small</symbolName>}"></div>
        <div [ngClass]="['<symbolName descr="CSS.CLASS_NAME">oy-chip--small</symbolName>', '<symbolName descr="CSS.CLASS_NAME">small</symbolName>']"></div>
    </inject>`,
   <symbolName descr="instance field">standalone</symbolName>: true,
   <symbolName descr="instance field">styles</symbolName>: `<inject descr="null">
      .<info descr="null">oy-chip</info> {
        &.<info descr="null">oy-chip--small</info> {
        }
      }
   </inject>`,
   <symbolName descr="instance field">host</symbolName>: {
     "<symbolName descr="HTML_ATTRIBUTE_NAME">class</symbolName>": "<symbolName descr="CSS.CLASS_NAME">oy-chip</symbolName> <symbolName descr="CSS.CLASS_NAME">oy-chip--small</symbolName>",
     "<symbolName descr="HTML_ATTRIBUTE_NAME">title</symbolName>": "some",
     "<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">[class.<symbolName descr="CSS.CLASS_NAME">oy-chip--small</symbolName>]</symbolName>": "<symbolName descr="instance field"><inject descr="null">small</inject></symbolName>",
     "<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">[attr.<symbolName descr="HTML_ATTRIBUTE_NAME">accesskey</symbolName>]</symbolName>": "<symbolName descr="instance field"><inject descr="null">small</inject></symbolName>",
     "<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">[style.<symbolName descr="CSS.PROPERTY_NAME">accent-color</symbolName>]</symbolName>": "<symbolName descr="instance field"><inject descr="null">small</inject></symbolName>",
     "<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">[bar]</symbolName>": "<symbolName descr="instance field"><inject descr="null">small</inject></symbolName>",
     '<symbolName descr="NG.EVENT_BINDING_ATTR_NAME">(keydown)</symbolName>': '<inject descr="null"><symbolName descr="instance method">onKeyDown</symbolName>(<symbolName descr="identifiers//global variable">$event</symbolName>)</inject>',
   },
   <symbolName descr="instance field">imports</symbolName>: [
     <symbolName descr="classes//exported class">NgClass</symbolName>
   ]
 })
export class <symbolName descr="classes//exported class">ChipComponent</symbolName> {
  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">HostBinding</symbolName>("<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">class.<symbolName descr="CSS.CLASS_NAME">oy-chip--small</symbolName></symbolName>")
  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">HostBinding</symbolName>("<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">attr.<symbolName descr="HTML_ATTRIBUTE_NAME">title</symbolName></symbolName>")
  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">HostBinding</symbolName>("<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">style.<symbolName descr="CSS.PROPERTY_NAME">align-content</symbolName></symbolName>")
  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">HostBinding</symbolName>("<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">bar</symbolName>")
  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Input</symbolName>('<symbolName descr="NG.PROPERTY_BINDING_ATTR_NAME">small</symbolName>')
  public <symbolName descr="instance field">small</symbolName>: <info descr="null">boolean</info> = false;

  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">HostListener</symbolName>('<symbolName descr="NG.EVENT_BINDING_ATTR_NAME">keydown</symbolName>', ['$event'])
  <symbolName descr="instance method">onKeyDown</symbolName>(<warning descr="Unused parameter $event"><weak_warning descr="TS6133: '$event' is declared but its value is never read."><symbolName descr="identifiers//parameter">$event</symbolName></weak_warning></warning>: <info descr="null">string</info>) {
  }
}
