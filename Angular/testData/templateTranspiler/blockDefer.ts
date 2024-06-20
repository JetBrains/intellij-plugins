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
    @defer (prefetch when user.name; no; on something) {
    
    } @placeholder (minimum 12; dd) {
    
    } @error {
    
    } @loading (max 12; after 12) {
    
    }
    <div #fooBar></div>
    @defer(prefetch;
           prefetch on;
           on foo;
           on timer ( ;
           on timer (12 ;
           on viewport ( foo ;
           on viewport ( fooBar ) ff;
           on viewport ( fooBar  ff;) {
           
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
