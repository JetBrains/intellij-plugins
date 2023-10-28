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
    @defer (prefetch when user.name; <error descr="'when', 'on', 'prefetch when' or 'prefetch on' trigger expected">n</error>o; on something) {
    
    } @placeholder (minimum 12; <error descr="'minimum' expected">d</error>d) {
    
    } @error {
    
    } @loading (<error descr="'minimum' or 'after' expected">m</error>ax 12; after 12) {
    
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
