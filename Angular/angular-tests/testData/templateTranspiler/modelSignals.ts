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
      [optional]="value3"
      (optionalChange)="value3 = $event"
      [required]="value3"
      (requiredChange)="value3 = $event"
      [foo]="12"
    ></app-test-signal>

    <!-- check signal assignability -->
    <app-test-annotation
        [(optional)]="signal1"
        [(required)]="signal2"
    ></app-test-annotation>
    <app-test-signal
        [(optional)]="signal1"
        [(required)]="signal2"
    ></app-test-signal>
    <app-test-annotation
        [(optional)]="signal3"
    ></app-test-annotation>
    <app-test-signal
        [(optional)]="signal3"
    ></app-test-signal>

    <!-- test readonly signal -->
    <app-test-annotation
        [(required)]="signal4"/>
    <app-test-annotation
        [(required)]="value4"/>
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
