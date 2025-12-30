import {Component} from '@angular/core';

export interface User {
  name: string,
  pictureUrl: string,
  isHuman?: boolean,
  isRobot?: boolean,
}

@Component({
  selector: 'robot-profile',
  standalone: true,
  template: `
    @if ( user.isRobot; as isRobot; <error descr="@if does not support parameter foo">foo</error>) {
        {{isRobot}} {{user.name}}
    } @else if (user.isHuman; <error descr="@else if does not support parameter as"><error descr="The as alias parameter in @else if block is supported only in Angular 20.2 and above.">as</error></error> isRobot) {
        {{<error descr="TS2339: Property 'isRobot' does not exist on type 'RobotProfileComponent'.">isRobot</error>}} {{user.name}}
    } @else {
        {{<error descr="TS2339: Property 'isRobot' does not exist on type 'RobotProfileComponent'.">isRobot</error>}} {{user.name}}
        @if(user.name) {
        }
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
