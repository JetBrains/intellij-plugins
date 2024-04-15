import {Component, EventEmitter, Input, model, Output, signal, computed} from '@angular/core';

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
    
    <!-- check signal assignability -->
    <app-test-annotation 
        [(optional)]="signal1" 
        [(required)]="<error descr="Type WritableSignal<number> is not assignable to type string | WritableSignal<string>...  Type WritableSignal<number> is not assignable to type WritableSignal<string>    Type number is not assignable to type string">signal2</error>"
    ></app-test-annotation>
    <app-test-signal 
        [(optional)]="signal1" 
        [(required)]="<error descr="Type WritableSignal<number> is not assignable to type string | WritableSignal<string>...  Type WritableSignal<number> is not assignable to type WritableSignal<string>    Type number is not assignable to type string">signal2</error>"
    ></app-test-signal>
    <app-test-annotation 
        [(optional)]="<error descr="Type Signal<number> is not assignable to type string | undefined | WritableSignal<string | undefined>...  Type Signal<number> is not assignable to type WritableSignal<string | undefined>    Type Signal<number> is not assignable to type string">signal3</error>" 
    ></app-test-annotation>
    <<error descr="Missing binding for required input required of component TestSignalComponent">app-test-signal</error> 
        [(optional)]="<error descr="Type Signal<number> is not assignable to type string | undefined | WritableSignal<string | undefined>...  Type Signal<number> is not assignable to type WritableSignal<string | undefined>    Type Signal<number> is not assignable to type string">signal3</error>" 
    ></app-test-signal>
    
    <!-- test readonly signal -->
    <app-test-annotation 
        [(required)]="signal4"/>
    <app-test-annotation 
        [(required)]="<error descr="Attempt to assign to const or readonly variable">value4</error>"/>
  `,
  styles: [],
})
export class AppComponent {
  value1: string = '';
  value2: string | undefined = undefined;
  value3: number = 12
  signal1 = signal("test")
  signal2 = signal(12)
  signal3 = computed(() => this.signal2() + 1)
  readonly signal4 = signal("test")
  readonly value4: string
}
