import {Component, Input} from '@angular/core';

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
     '[class.oy-chip--small]': 'small',
     '[class.oy-chip]': 'small',
     '[class.something]': 'small'
   },
 })
export class ChipComponent {
  @Input('small')
  public small: boolean = false;
}