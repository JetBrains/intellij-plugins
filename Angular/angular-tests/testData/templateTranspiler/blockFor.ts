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
        let last = $last, count = $count, as second; 
        as third
    ) {
        {{ item }} {{$first}} {{last}} {{$last}} {{count}} {{$foo}}
    }
    @for(item of array; track item.name;) {
      {{$first}} {{$last}} {{$even}} {{$odd}} {{$index}} {{$count}}
    }
    @for(; let a) {
      {{a}}
    }
  `
})
export class RobotProfileComponent {
    array!: Array<User>
}
