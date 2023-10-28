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
    @switch (user.name; <error descr="Unexpected token 'ff'">f</error>f) {
        @case ("foo") {
    
        }
        @case ("bar"; <error descr="Unexpected token 'foo'">f</error>oo) {
    
        }
        @default {
    
        }
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
