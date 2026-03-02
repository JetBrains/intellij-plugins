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
  selector: 'app-test-generic',
  standalone: true,
  template: ``
})
export class TestGenericComponent<T> {
  generic = model<T>('' as T);
  @Output()
  event = new EventEmitter<T>();
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TestAnnotationComponent, TestSignalComponent, TestGenericComponent],
  template: `
    <!-- two way syntax: -->
    <app-test-annotation [(optional)]="value2" [(required)]="value1"></app-test-annotation>
    <app-test-signal [(optional)]="value2" [(required)]="value1"></app-test-signal>

    <!-- split syntax: -->
    <app-test-annotation [optional]="value2" (optionalChange)="value2 = $event" [required]="value1" (requiredChange)="value2 = $event"></app-test-annotation>
    <app-test-signal [optional]="value2" (optionalChange)="value2 = $event" [required]="value1" (requiredChange)="value2 = $event"></app-test-signal>
    
    <!-- generic component -->
    <app-test-generic [(generic)]="signal1" (event)="<error descr="TS2322: Type 'string' is not assignable to type 'number'.">value3</error> = $event"></app-test-generic>
    <app-test-generic [(generic)]="signal2" (event)="value3 = $event"></app-test-generic>
    
    <!-- check types -->
    <app-test-signal 
      <error descr="TS2322: Type 'number' is not assignable to type 'string'.">[optional]</error>="value3" 
      (optionalChange)="<error descr="TS2322: Type 'string | undefined' is not assignable to type 'number'.
  Type 'undefined' is not assignable to type 'number'.">value3</error> = $event" 
      <error descr="TS2322: Type 'number' is not assignable to type 'string'.">[required]</error>="value3" 
      (requiredChange)="<error descr="TS2322: Type 'string' is not assignable to type 'number'.">value3</error> = $event"
      <error descr="Property foo is not provided by any applicable directives nor by <app-test-signal> element">[foo]</error>="12"
    ></app-test-signal>
    
    <!-- check signal assignability -->
    <app-test-annotation 
        [(optional)]="signal1" 
        <error descr="TS2322: Type 'number' is not assignable to type 'string'.">[(required)]</error>="signal2"
    ></app-test-annotation>
    <app-test-signal 
        [(optional)]="signal1" 
        <error descr="TS2322: Type 'number' is not assignable to type 'string'.">[(required)]</error>="signal2"
    ></app-test-signal>
    <app-test-annotation 
        <error descr="TS2322: Type 'Signal<number>' is not assignable to type 'string | undefined'.">[(optional)]</error>="signal3" 
    ></app-test-annotation>
    <<error descr="Missing binding for required input required of component TestSignalComponent">app-test-signal</error> 
        <error descr="TS2322: Type 'Signal<number>' is not assignable to type 'string | undefined'.">[(optional)]</error>="signal3" 
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
  readonly value4!: string
}
