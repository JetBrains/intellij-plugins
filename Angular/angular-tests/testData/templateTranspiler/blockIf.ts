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
        <a (click)="use(user.isRobot)">test</a>
    } @else if (user.isHuman; as isRobot) {
        {{isRobot}} {{user.name}}
        <a (click)="use(user.isHuman)">test</a>
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
    link?: ($event: UIEvent) => void;

    use(value: boolean) {

    }
}
