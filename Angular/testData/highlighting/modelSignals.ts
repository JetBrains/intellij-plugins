import {Component, EventEmitter, Input, model, Output} from '@angular/core';

@Component({
    selector: 'app-test-annotation',
    standalone: true,
    imports: [],
    template: ``,
    styles: ``
})
export class TestAnnotationComponent {
    @Input() optional?: string;
    @Output() optionalChange= new EventEmitter<string | undefined>();

    @Input() required!: string;
    @Output() requiredChange= new EventEmitter<string>();
}

@Component({
    selector: 'app-test-signal',
    standalone: true,
    imports: [],
    template: ``,
    styles: ``
})
export class TestSignalComponent {
    optional = model<string>();
    required = model.required<string>();
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TestAnnotationComponent, TestSignalComponent],
  template: `
    <!-- two way syntax: -->
    <app-test-annotation [(optional)]="value2" [(required)]="value1"></app-test-annotation>
    <app-test-signal [(optional)]="value2" [(required)]="value1"></app-test-signal>

    <!-- split syntax: -->
    <app-test-annotation [optional]="value2" (optionalChange)="value2 = $event" [required]="value1" (requiredChange)="value2 = $event"></app-test-annotation>
    <app-test-signal [optional]="value2" (optionalChange)="value2 = $event" [required]="value1" (requiredChange)="value2 = $event"></app-test-signal>
    
    <!-- check types -->
    <app-test-signal 
      [optional]="<error descr="Type number is not assignable to type string | undefined  Type number is not assignable to type string">value3</error>" 
      (optionalChange)="value3 = <error descr="Assigned expression type string | undefined is not assignable to type number  Type string is not assignable to type number">$event</error>" 
      [required]="<error descr="Type number is not assignable to type string">value3</error>" 
      (requiredChange)="value3 = <error descr="Assigned expression type string is not assignable to type number">$event</error>"
      <error descr="Property foo is not provided by any applicable directives nor by <app-test-signal> element">[foo]</error>="12"
    ></app-test-signal>
    
  `,
  styles: [],
})
export class AppComponent {
  value1: string = '';
  value2: string | undefined = undefined;
  value3: number = 12
}
