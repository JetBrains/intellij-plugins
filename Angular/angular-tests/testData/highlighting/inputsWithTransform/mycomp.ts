import {booleanAttribute, Component, Input} from '@angular/core';

@Component({
    inputs: [
        { name: "obj", transform: (value: { foo: number }): boolean => Boolean(value) },
        { name: "obj_virtual", transform: (value: { foo: number }): boolean => Boolean(value) }
    ],
    selector: 'app-test',
    template: `
    <p *ngIf="nope">test works!</p>
    <p *ngIf="alsoNo">test also works!</p>
  `
})
export class TestComponent {
    @Input({ transform: booleanAttribute}) nope!: boolean;
    @Input({transform: (value: string): boolean => Boolean(value)}) alsoNo!: boolean;
    @Input({transform: (value: 'true' | 'false'): boolean => Boolean(value)}) strict!: boolean;
    obj!: boolean;

}
