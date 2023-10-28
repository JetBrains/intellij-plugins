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
        let first = $first, as<error descr="'=' expected"> </error>second; 
        <error descr="'let' or 'track' expected">a</error>s third
    ) {
        {{ item }} {{first}} {{$last}} {{$count}} {{<error descr="Unresolved variable or type $foo">$foo</error>}}
    }
  `
})
export class RobotProfileComponent {
    array!: Array<User>
}
