import {Component, Input} <info textAttributesKey="TS.KEYWORD">from</info> '@angular/core';

<info textAttributesKey="TS.DECORATOR">@</info><info textAttributesKey="TS.DECORATOR">Component</info>({
   selector: 'oy-chip',
   template: ``,
   styles: `<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">
        .<info textAttributesKey="CSS.CLASS_NAME">oy-chip</info> {
            &.<info textAttributesKey="CSS.CLASS_NAME">oy-chip--small</info> {
            }
            &<warning descr="Selector oy-chip--unused is never used">.<info textAttributesKey="CSS.CLASS_NAME">oy-chip--unused</info></warning> {
            }
        }
        <warning descr="Selector oy-chip-unused is never used">.<info textAttributesKey="CSS.CLASS_NAME">oy-chip-unused</info></warning> {
            
        }
        :host(.<info textAttributesKey="CSS.CLASS_NAME">something</info>) {
            color: <info textAttributesKey="INFORMATION_ATTRIBUTES">red</info>;
        }
        :host(<warning descr="Selector unused is never used">.<info textAttributesKey="CSS.CLASS_NAME">unused</info></warning>) {
            color: <info textAttributesKey="INFORMATION_ATTRIBUTES">red</info>;
        }
    </inject>`,
   host: {
     '<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[class.oy-chip--small]</info>': '<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">small</inject>',
     '<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[class.oy-chip]</info>': '<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">small</inject>',
     '<info textAttributesKey="NG.PROPERTY_BINDING_ATTR_NAME">[class.something]</info>': '<inject textAttributesKey="TypeScript:INJECTED_LANGUAGE_FRAGMENT">small</inject>'
   },
 })
export class ChipComponent {
  <info textAttributesKey="TS.DECORATOR">@</info><info textAttributesKey="TS.DECORATOR">Input</info>('small')
  public small: <info textAttributesKey="TS.PRIMITIVE.TYPES">boolean</info> = false;
}