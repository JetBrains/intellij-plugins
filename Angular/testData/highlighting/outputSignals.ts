import {Component, EventEmitter, output, Output} from '@angular/core';
import {outputFromObservable} from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-test-annotation',
    standalone: true,
    imports: [],
    template: ``,
    styles: ``
})
export class TestAnnotationComponent {
    @Output() output= new EventEmitter<string>();
}

@Component({
    selector: 'app-test-signal',
    standalone: true,
    imports: [],
    template: ``,
    styles: ``
})
export class TestSignalComponent {
  output = output<string>();
  onNameChange$ = new Observable<string>();
  onNameChange = outputFromObservable(this.onNameChange$, {alias: "outputFromObservable"});
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TestAnnotationComponent, TestSignalComponent],
  template: `
    <!-- split syntax: -->
    <app-test-annotation (output)="value2 = $event"></app-test-annotation>
    <app-test-signal (output)="value2 = $event"/>
    <app-test-signal (outputFromObservable)="value2 = $event"/>
    
    <!-- check types -->
    <app-test-signal  
      (output)="<error descr="TS2322: Type 'string' is not assignable to type 'number'.">value3</error> = $event"
      (outputFromObservable)="<error descr="TS2322: Type 'string' is not assignable to type 'number'.">value3</error> = $event"
      <error descr="Property foo is not provided by any applicable directives nor by <app-test-signal> element">[foo]</error>="12"
    />
  `,
  styles: [],
})
export class AppComponent {
  value2: string | undefined = undefined;
  value3: number = 12
}
