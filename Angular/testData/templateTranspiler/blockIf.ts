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
    @if ( user.isRobot; as isRobot; foo) {
        {{isRobot}} {{user.name}}
    } @else if (user.isHuman; as isRobot) {
        {{isRobot}} {{user.name}}
    } @else {
        {{isRobot}} {{user.name}}
        @if(user.name) {
          {{user.name}}
        }
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
