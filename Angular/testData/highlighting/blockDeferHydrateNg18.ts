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
    @defer (on immediate; <error descr="@defer does not support parameter prefix hydrate">hydrate</error> on interaction) {
      ...
    }
    @defer (<error descr="@defer does not support parameter prefix hydrate">hydrate</error> on hover) {
      @defer (<error descr="@defer does not support parameter prefix hydrate">hydrate</error> when <error descr="TS2349: This expression is not callable.
  Type 'User' has no call signatures.">user</error>() !== null) {
        ...
      }
    }
    @defer (<error descr="@defer does not support parameter prefix hydrate">hydrate</error> <error descr="@defer hydrate does not support parameter never">never</error>) {
       ...
    }
    @defer (<error descr="@defer does not support parameter prefix hydrate">hydrate</error><error descr="Expected 'when', 'on' or 'never'"> </error><error descr="@defer hydrate does not support parameter nver">nver</error>) {
       ...
    }
    @defer (<error descr="@defer does not support parameter prefix hydrate">hydrate</error><error descr="Expected 'when', 'on' or 'never'">)</error> {
       ...
    }
    @defer (<error descr="@defer does not support parameter hdrate">hdrate</error> never) {
       ...
    }
    @defer (prefetch<error descr="Expected 'when' or 'on'"> </error><error descr="@defer prefetch does not support parameter never">never</error>) {
       ...
    }
    @defer (prefetch<error descr="Expected 'when' or 'on'">)</error> {
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
