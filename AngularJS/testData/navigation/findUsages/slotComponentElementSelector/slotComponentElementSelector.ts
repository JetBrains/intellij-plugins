import {Component} from "@angular/core";

@Component({
  selector: "slots-component",
  template: `
      <ng-content select="tag<caret>-slot"></ng-content>
  `
})
export class SlotsComponent {

  constructor() {
  }

}
