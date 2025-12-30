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
    @switch (user.name; <error descr="@switch does not support parameter ff">ff</error>) {
        @case ("foo") {
    
        }
        @case ("bar"; <error descr="@case does not support parameter foo">foo</error>) {
    
        }
        @default {
    
        }
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
