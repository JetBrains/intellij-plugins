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
        let first = $first, as<error descr="'=' expected"> </error><error descr="Unresolved variable or type second">second</error>; 
        <error descr="@for does not support parameter as">as</error> third
    ) {
        {{ item }} {{first}} {{<error descr="Unresolved variable or type $first">$first</error>}} {{$last}} {{$count}} {{<error descr="Unresolved variable or type $foo">$foo</error>}}
    }
    @for(item<error descr="'of' expected"> </error>array; 
         track<error descr="Expression expected">;</error> 
         let<error descr="Identifier expected">)</error> {}
    <error descr="@for requires track parameter">@for</error>(<error descr="Expression expected"><error descr="Identifier expected">;</error></error> let a<error descr="'=' expected"><error descr="Identifier expected">)</error></error> {
    }     
  `
})
export class RobotProfileComponent {
    array!: Array<User>
}
