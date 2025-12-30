import {Component} from '@angular/core';

@Component({
  selector: 'robot-profile',
  standalone: true,
  template: `
    {{ 12 ** 33 ** value }}
    <div [title]=" 12 ** 33 ** value"></div>
    <div title="foo-{{ 12 ** 33 ** value}}"></div>
 `
})
export class RobotProfileComponent {
  value!: string | number
}
