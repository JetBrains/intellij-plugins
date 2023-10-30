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
    @for (
        item of array; 
        track item.name; 
        let first = $first, ev = $even, co = $count;
    ) {
        {{ item }} {{first}} {{ev}} {{co}} {{<error descr="Unresolved variable or type $foo">$foo</error>}}
    }
  `
})
export class RobotProfileComponent {
    array!: Array<User>
}
