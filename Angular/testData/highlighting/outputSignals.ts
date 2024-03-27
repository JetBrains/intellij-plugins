import {Component, EventEmitter, output, Output} from '@angular/core';

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
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TestAnnotationComponent, TestSignalComponent],
  template: `
    <!-- split syntax: -->
    <app-test-annotation (output)="value2 = $event"></app-test-annotation>
    <app-test-signal (output)="value2 = $event"></app-test-signal>
    
    <!-- check types -->
    <app-test-signal  
      (output)="value3 = <error descr="Assigned expression type string is not assignable to type number">$event</error>"
      <error descr="Property foo is not provided by any applicable directives nor by <app-test-signal> element">[foo]</error>="12"
    ></app-test-signal>
    
  `,
  styles: [],
})
export class AppComponent {
  value2: string | undefined = undefined;
  value3: number = 12
}
