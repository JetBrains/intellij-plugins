import {Component} from "@angular/core";

@Component({
  selector: "slots-component",
  template: `
      <ng-content select="tag-slot"></ng-content>
      <ng-content select="[attr-slot]"></ng-content>
  `
})
export class SlotsComponent {

  constructor() {
  }

}
