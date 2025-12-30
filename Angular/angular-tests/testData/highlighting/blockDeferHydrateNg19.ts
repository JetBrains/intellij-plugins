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
    @defer (on immediate; hydrate on interaction) {
      ...
    }
    @defer (hydrate on hover) {
      @defer (hydrate when <error descr="TS2349: This expression is not callable.
  Type 'User' has no call signatures.">user</error>() !== null) {
        ...
      }
    }
    @defer (hydrate never) {
       ...
    }
    @defer (hydrate never; hydrate <error descr="Cannot specify additional hydrate triggers if hydrate never is present">on</error> hover) {
       ...
    }
    @defer (hydrate<error descr="Expected 'when', 'on' or 'never'"> </error><error descr="@defer hydrate does not support parameter nver">nver</error>) {
       ...
    }
    @defer (<error descr="@defer does not support parameter hdrate">hdrate</error> never) {
       ...
    }
    @defer (prefetch<error descr="Expected 'when' or 'on'"> </error><error descr="@defer prefetch does not support parameter never">never</error>) {
       ...
    }
    @defer (<error descr="@defer does not support parameter never">never</error>) {
       ...
    }
    @defer (<error descr="@defer does not support parameter nver">nver</error>) {
       ...
    }
  `
})
export class RobotProfileComponent {
    user!: User
}
