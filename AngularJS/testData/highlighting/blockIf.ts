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
    @if ( user.isRobot; as isRobot; <error descr="'as' expected">f</error>oo) {
        {{isRobot}} {{user.name}}
    } @else if (user.isHuman; <error descr="Unexpected token 'as'">a</error>s isRobot) {
        {{<error descr="Unresolved variable or type isRobot">isRobot</error>}} {{user.name}}
    } @else {
        {{<error descr="Unresolved variable or type isRobot">isRobot</error>}} {{user.name}}
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
