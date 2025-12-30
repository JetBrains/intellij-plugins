import {Component} from '@angular/core';

@Component({
  selector: 'robot-profile',
  standalone: true,
  template: `
    {{void check(true)}}
    <div [title]="void check(true)"></div>
    <div title="foo-{{void check(true)}}"></div>
 `
})
export class RobotProfileComponent {
  value!: string | number

  check (value: boolean) {

  }
}
