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
    @defer (prefetch when user.name; <error descr="@defer does not support parameter no">no</error>; on something) {
    
    } @placeholder (minimum 12; <error descr="@placeholder does not support parameter dd">dd</error><error descr="Numeric literal expected">)</error> {
    
    } @error {
    
    } @loading (<error descr="@loading does not support parameter max">max</error> 12; after 12) {
    
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
