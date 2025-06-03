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
    @switch (user.name; ff) {
        @case ("foo") {
            {{user.name}}
            <a (click)="use(user.name)">test</a>
        }
        @case ("bar"; foo) {
            {{user.name}}
            <a (click)="use(user.name)">test</a>
        }
        @default {
            {{user.name}}
            <a (click)="use(user.name)">test</a>
        }
    }
  `
})
export class RobotProfileComponent {
    user!: User

    use(value: "foo") {

    }
}
