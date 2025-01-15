import {Component} from '@angular/core';

@Component({
   selector: 'oy-chip',
   template: ``,
   styles: `
        .oy-chip {
            &.oy-chip--small {
            }
            &<warning descr="Selector oy-chip--unused is never used">.oy-chip--unused</warning> {
            }
        }
        <warning descr="Selector oy-chip-unused is never used">.oy-chip-unused</warning> {

        }
        :host(.something) {
            color: red;
        }
        :host(<warning descr="Selector unused is never used">.unused</warning>) {
            color: red;
        }
    `,
   host: {
     'class': 'oy-chip oy-chip--small something',
   },
 })
export class ChipComponent {
}