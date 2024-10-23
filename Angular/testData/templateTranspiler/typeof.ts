import {Component} from '@angular/core';

@Component({
 selector: 'robot-profile',
 standalone: true,
 template: `
    @if (typeof value === 'string') {
      {{check(value)}}
    } @else {
      {{check(value)}}
    }
 `
})
export class RobotProfileComponent {
  value!: string | number

  check (value: boolean) {

  }
}
