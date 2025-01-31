import {Component,} from '@angular/core';
import {NgForOf} from "@angular/common";

@Component({
  selector: 'test',
  template: `
        <div *ngFor="let foo of [1,2,3]; trackBy: trackByFn; let index = index"></div>
        <ng-template #foo></ng-template>
    `,
  imports: [
    NgForOf
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

