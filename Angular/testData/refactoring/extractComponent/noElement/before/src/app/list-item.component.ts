import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Person } from './Person';

@Component({
  selector: 'app-list-item',
  templateUrl: './list-item.component.html',
})
export class ListItemComponent {
  @Input() person?: Person;

  @Input() checked = false;

  @Input() label: string;

  @Input() twoWay: number;

  @Output() twoWayChange = new EventEmitter<number>();

  @Input()
  set accessor(x: string) {
  }

  get accessor(): string {
    return 'const';
  }

}

