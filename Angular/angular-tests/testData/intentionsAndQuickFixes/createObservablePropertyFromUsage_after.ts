import {Component, Input, signal} from '@angular/core';
import {AsyncPipe} from "@angular/common";

@Component({
   selector: 'app-test',
   standalone: true,
   template: ``
 })
export class TestComponent {
  @Input() test1!: boolean;
}

@Component({
   standalone: true,
   selector: 'app-root',
   imports: [AsyncPipe, TestComponent],
   template: `
      <app-test [test1]="foo | async"/>`
})
export class AppComponent {
    protected foo<caret>: Observable<boolean>;

}
