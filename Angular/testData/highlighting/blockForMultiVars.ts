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
        {{ item }} {{first}} {{ev}} {{co}} {{<error descr="TS2339: Property '$foo' does not exist on type 'RobotProfileComponent'.">$foo</error>}}
    }
  `
})
export class RobotProfileComponent {
    array!: Array<User>
}
