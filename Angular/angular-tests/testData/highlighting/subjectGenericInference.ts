import {Component} from '@angular/core';
import {Subject} from "rxjs";
import {AsyncPipe, NgIf} from "@angular/common";

type Greeting = {
  hello: string;
}

type <warning descr="Unused type alias Greeting2"><weak_warning descr="TS6196: 'Greeting2' is declared but never used.">Greeting2</weak_warning></warning> = {
  foo: string;
}

@Component({
 selector: 'greeting',
 standalone: true,
 imports: [AsyncPipe, NgIf],
 template: `
    <ng-container *ngIf="greeting$ | async as greeting">
      {{ greeting.hello }}
      {{ greeting.<error descr="TS2339: Property 'foo' does not exist on type 'Greeting'.">foo</error> }}
    </ng-container>
  `,
})
export class GreetingComponent {
  greeting$ = new Subject<Greeting>() ;
}