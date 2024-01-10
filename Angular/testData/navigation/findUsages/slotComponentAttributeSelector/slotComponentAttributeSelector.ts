import {Component} from "@angular/core";

@Component({
  selector: "slots-component",
  template: `
      <ng-content select="[attr<caret>-slot]"></ng-content>
  `
})
export class SlotsComponent {
}
