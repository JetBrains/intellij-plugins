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
        let first = $first, <warning descr="Unused constant as">as</warning><error descr="'=' expected"> </error>second; 
        <error descr="@for does not support parameter as">as</error> third
    ) {
        {{ item }} {{first}} {{<error descr="TS2339: Property '$first' does not exist on type 'RobotProfileComponent'.">$first</error>}} {{$last}} {{$count}} {{<error descr="TS2339: Property '$foo' does not exist on type 'RobotProfileComponent'.">$foo</error>}}
    }
    @for(<warning descr="Unused constant item">item</warning><error descr="'of' expected"> </error>array; 
         track<error descr="Expression expected">;</error> 
         let<error descr="Identifier expected">)</error> {}
    <error descr="@for requires track parameter">@for</error>(<error descr="Expression expected"><error descr="Identifier expected">;</error></error> let <warning descr="Unused constant a">a</warning><error descr="'=' expected"><error descr="Identifier expected">)</error></error> {
    }     
  `
})
export class RobotProfileComponent {
    array!: Array<User>
}
