// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, EventEmitter, Input, Output} from '@angular/core';

@Directive({
    selector: "[gen]",
    standalone: true,
})
export class GenericsDirective<T, S> {

    @Input()
    input1!: T;

    @Input()
    input2!: T;

    @Input()
    input3!: S;

    @Output()
    output1!: EventEmitter<T>

    @Output()
    output3!: EventEmitter<S>

}

@Directive({
    selector: "[gen2]",
    standalone: true,
})
export class GenericsDirective2<T> {

    @Input()
    input1!: T;

    @Input()
    input2!: string;

}

@Component({
    selector: "app-test",
    template: `
    <div gen 
         [input1]="12"
         [input2]="20" 
         [input3]="'foo'" 
         (output1)="useNumber($event)" 
         (output3)="useString($event)">
    </div>
            
    <div gen 
         [input1]="true" 
         <error descr="TS2322: Type 'string' is not assignable to type 'boolean'." textAttributesKey="ERRORS_ATTRIBUTES">[input2]</error>="'20'" 
         [input3]="12" 
         (output1)="useNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)" 
         (output3)="useString(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>
         
    <div gen
         [input1]="12"
         <error descr="TS2322: Type 'string' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">[input2]</error>="'20'"
         [input3]="'bar'"
         (output1)="useNumber($event)" 
         (output3)="useString($event)">
    </div>
         
    <div gen 
         [input1]="[1,2,3]" 
         [input2]="[4,5]" 
         (output1)="useNumber($event[0])">
    </div>
         
    <div gen 
         [input1]="[1,2,3]" 
         [input2]="[4,5]" 
         (output1)="useNumber(<error descr="TS2345: Argument of type 'number[]' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>
    
    <div gen
         input1
         (output1)="useNumber($event)">
    </div>

    <div gen
         input1
         input2
         (output1)="useNumber(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>

    <div gen
         <warning descr="[input1] requires value" textAttributesKey="WARNING_ATTRIBUTES">[input1]</warning>
         (output1)="useString($event)">
    </div>
    
    <div gen
         [input1]=""
         input2
         (output1)="<error descr="TS1345: An expression of type 'void' cannot be tested for truthiness." textAttributesKey="ERRORS_ATTRIBUTES">useNumber(<error descr="TS2345: Argument of type 'string | undefined' is not assignable to parameter of type 'number'.
  Type 'undefined' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)</error> && useString(<error descr="TS2345: Argument of type 'string | undefined' is not assignable to parameter of type 'string'.
  Type 'undefined' is not assignable to type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>

    <div gen
         input1
         [input2]=""
         (output1)="useNumber(<error descr="TS2345: Argument of type 'string | undefined' is not assignable to parameter of type 'number'.
  Type 'undefined' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>
    
    <div gen gen2 
         [input1]="12" 
         <error descr="TS2322: Type 'number' is not assignable to type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">[input2]</error>="12">
    </div>
         
    <div gen gen2 
         [input1]="'foo'" 
         <error descr="TS2322: Type 'number' is not assignable to type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">[input2]</error>="12">
    </div>
    
    <div gen gen2 
         [input1]="12" 
         <error descr="TS2322: Type 'string' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">[input2]</error>="'foo'">
    </div>
    
    <div gen gen2 
         [input1]="'foo'" 
         [input2]="'foo'">
    </div>
    
    `,
    standalone: true,
    imports: [GenericsDirective, GenericsDirective2]
})
export class TestComponent {

    useNumber(<warning descr="Unused parameter val" textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><weak_warning descr="TS6133: 'val' is declared but its value is never read." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES">val</weak_warning></warning>: number) {

    }

    useString(<warning descr="Unused parameter val" textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES"><weak_warning descr="TS6133: 'val' is declared but its value is never read." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES">val</weak_warning></warning>: string) {

    }

}
