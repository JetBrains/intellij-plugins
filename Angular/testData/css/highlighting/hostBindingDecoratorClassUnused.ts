import {Component, HostBinding} from '@angular/core';

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
 })
export class ChipComponent {
  @HostBinding('class.oy-chip')
  title1 = 'test-pipe';

  @HostBinding('class.oy-chip--small')
  title2 = 'test-pipe';

  @HostBinding('class.something')
  title3 = 'test-pipe';
}