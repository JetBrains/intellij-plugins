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
    @switch (user.name; <error descr="@switch does not support parameter ff">ff</error>) {
        @case ("foo") {
            <a (click)="use(user.name)">test</a>
        }
        @case ("foo1")
        @case ("bar"; <error descr="@case does not support parameter foo">foo</error>) {
            <a (click)="use(<error descr="TS2345: Argument of type '\"foo1\" | \"bar\"' is not assignable to parameter of type '\"foo\"'.
  Type '\"foo1\"' is not assignable to type '\"foo\"'.">user.name</error>)">test</a>
        }
        @default {
            <a (click)="use(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type '\"foo\"'.">user.name</error>)">test</a>
        }
    }
    <error descr="@case block must be nested under primary block @switch">@case</error>("foo2")<EOLError descr="Incomplete @case block - expected { or another @case or @default condition"></EOLError>
    <error descr="@else block must be a sibling of primary block @if">@else</error> {
    
    }
    @switch (<error descr="TS2339: Property 'foo' does not exist on type 'RobotProfileComponent'.">foo</error>) {
       @default
       @case(1) {    
       }
    }
    @switch (<error descr="TS2339: Property 'foo' does not exist on type 'RobotProfileComponent'.">foo</error>) {
       <error descr="@switch block can only have one @default block">@default</error>
       <error descr="@switch block can only have one @default block">@default</error> {     
       }
    }
  `
})
export class RobotProfileComponent {
  user!: User
  use(<warning descr="Unused parameter value"><weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning></warning>: "foo") {

  }
}
