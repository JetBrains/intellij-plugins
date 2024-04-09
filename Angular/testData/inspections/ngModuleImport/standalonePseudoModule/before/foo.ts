import {Component} from '@angular/core';

@Component({
  selector: 'foo',
  standalone: true
})
export class FooComponent{

}

export declare const FOO_COMPONENT_EXPORT_DECLARE_CONST_READ_ONLY: readonly [typeof FooComponent]
export declare const FOO_COMPONENT_EXPORT_DECLARE_CONST: [typeof FooComponent]

export const FOO_COMPONENT_EXPORT_CONST = [FooComponent];
export const FOO_COMPONENT_EXPORT_CONST_AS_CONST = [FooComponent] as const;
