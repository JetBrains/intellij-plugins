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
      <div class="foo"/>
      <div [class.foo<caret>]="small"></div>
      <div [ngClass]="{'foo' : small}"></div>
      <div [ngClass]="['foo', 'small']"></div>
  `,
  standalone: true,
  styles: `
      .foo {
      }
  `,
  host: {
    "class" : "foo",
    "[class.foo]": "small",
  },
  imports: [
    NgClass
  ]
})
export class ChipComponent {
  @HostBinding("class.foo")
  public small: boolean = false;
}
