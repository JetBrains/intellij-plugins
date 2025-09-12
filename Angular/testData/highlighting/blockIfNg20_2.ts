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
    @if ( user.isRobot; as isRobot; <error descr="@if does not support parameter foo">foo</error>) {
        {{isRobot}} {{user.name}}
        <a (click)="use(isRobot)">test</a>
        <a (click)="prevent(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'string'.">isRobot</error>)">test</a>
    } @else if (user.isHuman; as isRobot) {
        {{isRobot}} {{user.name}}
        <a (click)="use(isRobot)">test</a>
        <a (click)="prevent(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'string'.">isRobot</error>)">test</a>
    } @else {
        {{<error descr="TS2339: Property 'isRobot' does not exist on type 'RobotProfileComponent'.">isRobot</error>}} {{user.name}}
        @if(user.name) {
          {{user.name}}
        }
    }
  `
})
export class RobotProfileComponent {
    user!: User;

    use(<warning descr="Unused parameter value"><weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning></warning>: boolean) {

    }
    prevent(<warning descr="Unused parameter value"><weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning></warning>: string) {

    }
}
