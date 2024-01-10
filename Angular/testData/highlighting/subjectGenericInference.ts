import {Component} from '@angular/core';
import {Subject} from "rxjs";
import {AsyncPipe, NgIf} from "@angular/common";

type Greeting = {
  hello: string;
}

type <warning descr="Unused type alias Greeting2">Greeting2</warning> = {
  foo: string;
}

@Component({
 selector: 'greeting',
 standalone: true,
 imports: [AsyncPipe, NgIf],
 template: `
    <ng-container *ngIf="greeting$ | async as greeting">
      {{ greeting.hello }}
      {{ greeting.<error descr="Unresolved variable foo">foo</error> }}
    </ng-container>
  `,
})
export class GreetingComponent {
  greeting$ = new Subject<Greeting>() ;
}