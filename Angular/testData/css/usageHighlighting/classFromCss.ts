import {Component, Input, HostBinding} from '@angular/core';

@Component({
   selector: 'oy-chip',
   template: `
      <div class="<usage>oy-chip--small</usage>"></div>
      <div [class.<usage>oy-chip--small</usage>]="small"></div>
      <div [ngClass]="{'<usage>oy-chip--small</usage>' : small}"></div>
   `,
   styles: `
        .oy-chip {
            &.<usage>oy-chip<caret>--small</usage> {
            }
        }
        .<usage>oy-chip--small</usage> {
        }
    `,
   host: {
     "class": "oy-chip <usage>oy-chip--small</usage> something",
     "[class.<usage>oy-chip--small</usage>]": "small",
   },
 })
export class ChipComponent {
  @Input('small')
  public small: boolean = false;

  @HostBinding("class.<usage>oy-chip--small</usage>")
  public hostSmall: boolean = false;
}