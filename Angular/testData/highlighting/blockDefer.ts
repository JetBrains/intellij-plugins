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
    @defer (prefetch when user.name; <error descr="@defer does not support parameter no">no</error>; on <error descr="Unresolved symbol">something</error>) {
    
    } @placeholder (minimum 12; <error descr="@placeholder does not support parameter dd">dd</error><error descr="Numeric literal expected">)</error> {
    
    } @error {
    
    } @loading (<error descr="@loading does not support parameter max">max</error> 12; after 12) {
    
    }
    <div #fooBar></div>
    @defer(prefetch<error descr="Expected 'when' or 'on'">;</error>
           prefetch on<error descr="Identifier expected">;</error>
           on <error descr="Unresolved symbol">foo</error>;
           on timer (<error descr=") expected"> </error>;
           on timer (12<error descr=") expected"> </error>;
           on viewport ( <error descr="Unresolved symbol">foo</error><error descr=") expected"> </error>;
           on viewport ( fooBar )<error descr="Unexpected token ff"> </error>ff;
           on viewport ( fooBar<error descr=") expected"> </error> ff;) {
           
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
