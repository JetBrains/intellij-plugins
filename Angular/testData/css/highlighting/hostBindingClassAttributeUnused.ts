import {Component} <info textAttributesKey="TS.KEYWORD">from</info> '@angular/core';

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
     '<info textAttributesKey="HTML_ATTRIBUTE_NAME">class</info>': '<info textAttributesKey="CSS.CLASS_NAME">oy-chip oy-chip--small something</info>',
   },
 })
export class ChipComponent {
}