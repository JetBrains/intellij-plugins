import {Component} from '@angular/core';

@Component({
  selector: 'robot-profile',
  standalone: true,
  template: `
    <div (keydown)="$event.foo"></div>
    <div (keydown.space)="$event.foo"></div>
    <div (keyup.code.123)="$event.foo"></div>
 `
})
export class RobotProfileComponent {
}
