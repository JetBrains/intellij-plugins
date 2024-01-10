import { Component, EventEmitter, Input, Output } from '@angular/core';

/**
 * component comment
 */
@Component({
  selector: 'app-list-item',
  template: `item`,
})
export class ListItemComponent {
  /**
   * checked comment
   */
  @Input() checked = false;

  /**
   * label comment
   */
  @Input() label: string;

  /**
   * title comment (global attr shadowing)
   */
  @Input() title: string;

  /**
   * twoWay comment
   */
  @Input() twoWay: number;

  /**
   * twoWayChange comment
   */
  @Output() twoWayChange = new EventEmitter<number>();

  constructor() {
  }
}



