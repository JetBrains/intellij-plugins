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
        }
        @case ("bar"; foo) {
            {{user.name}}
        }
        @default {
            {{user.name}}
        }
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
