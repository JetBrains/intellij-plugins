import {Component, Directive, EventEmitter, Input, Output} from '@angular/core';

@Directive({
 selector: '[test]',
 standalone: true
})
class TestDirective {
  @Input()
  test!: string;

  @Output()
  close = new EventEmitter();
}

@Component({
 selector: 'app-root',
 standalone: true,
 imports: [
   TestDirective
 ],
 template: `
    <ng-template 
     [test]="'hello world'"
     (close)="$event" 
     <error descr="Event click is not emitted by any applicable directive on an embedded template">(click)</error>="$event" 
     <error descr="Property foo is not provided by any applicable directive on an embedded template">[foo]</error>="12"
    >
      Hello
    </ng-template>
  `,
})
export class AppComponent {
}
