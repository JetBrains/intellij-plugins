import {
  Component,
  HostBinding,
  HostListener,
  Input,
} from '@angular/core';
import {NgClass} from "@angular/common";

@Component({
  selector: 'oy-chip',
  template: `
        <div class="oy-chip"/>
        <div [class.oy-ch<caret>ip]="small"></div>
        <div [ngClass]="{'oy-chip' : small}"></div>
        <div [ngClass]="['oy-chip', 'small']"></div>
    `,
  standalone: true,
  styles: `
      .oy-chip {
      }
   `,
  host: {
    "class" : "oy-chip",
    "[class.oy-chip]": "small",
  },
  imports: [
    NgClass
  ]
})
export class ChipComponent {
  @HostBinding("class.oy-chip")
  public small: boolean = false;
}
