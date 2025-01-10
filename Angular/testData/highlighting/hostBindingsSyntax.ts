import {
  Component,
  Directive,
  HostBinding,
  HostListener,
  Input,
} <info textAttributesKey="TS.KEYWORD">from</info> '@angular/core';
import {NgClass} <info textAttributesKey="TS.KEYWORD">from</info> "@angular/common";

<info descr="decorator">@</info><info descr="decorator">Directive</info>({
   selector: '[appClicks]',
   standalone: true,
   host: {
     '<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[title]</info>': '<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">foo</inject>',
     '<info textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">(click)</info>': '<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">onClick()</inject>'
   }
 })
export <info textAttributesKey="TS.KEYWORD">abstract</info> class AppClicksDirective {
  foo!: <info textAttributesKey="TS.PRIMITIVE.TYPES">string</info>
  onClick() {
    console.log('Click');
  }
}

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   selector: 'oy-chip',
   template: `<inject>
        <div [class.oy-chip--small]="small"></div>
        <div [ngClass]="{'oy-chip--small' : small}"></div>
        <div [ngClass]="['oy-chip--small', 'small']"></div>
    </inject>`,
   standalone: true,
   styles: `<inject>
      .<info textAttributesKey="CSS.CLASS_NAME">oy-chip</info> {
        &.<info textAttributesKey="CSS.CLASS_NAME">oy-chip--small</info> {
        }
      }
   </inject>`,
   host: {
     "<info textAttributesKey="HTML_ATTRIBUTE_NAME">class</info>": "<info textAttributesKey="CSS.CLASS_NAME">oy-chip oy-chip--small</info>",
     "<info textAttributesKey="HTML_ATTRIBUTE_NAME">title</info>": "some",
     "<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[class.oy-chip--small]</info>": "<inject>small</inject>",
     "<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[attr.accesskey]</info>": "<inject>small</inject>",
     "<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[style.accent-color]</info>": "<inject>small</inject>",
     "<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[bar]</info>": "<inject>small</inject>",
     '<info textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">(keydown)</info>': '<inject>onKeyDown($event)</inject>',
   },
   imports: [
     NgClass
   ]
 })
export class ChipComponent {
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">class.oy-chip--small</info>")
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">attr.title</info>")
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">style.align-content</info>")
  <info descr="decorator">@</info><info descr="decorator">HostBinding</info>("<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">bar</info>")
  <info descr="decorator">@</info><info descr="decorator">Input</info>('small')
  public small: <info textAttributesKey="TS.PRIMITIVE.TYPES">boolean</info> = false;

  <info descr="decorator">@</info><info descr="decorator">HostListener</info>('<info textAttributesKey="NG.EVENT_BINDING_ATTR_NAME">keydown</info>', ['$event'])
  onKeyDown(<warning descr="Unused parameter $event"><weak_warning descr="TS6133: '$event' is declared but its value is never read.">$event</weak_warning></warning>: <info textAttributesKey="TS.PRIMITIVE.TYPES">string</info>) {
  }
}
