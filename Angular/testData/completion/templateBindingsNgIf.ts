import {Component} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'test',
  template: `
        <div *ngIf="true as foo; else foo as sss; let car = ngIf"></div>
        <ng-template #foo></ng-template>
    `,
  imports: [
    NgIf
  ]
})
export class ChipComponent {

  condition!: boolean;

  items = [1, 2, 3];

  onKeyDown($event: string) {
  }

  trackByFn(index: number, item: number) {
    return index;
  }
}

