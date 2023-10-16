// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, EventEmitter, Input, Output} from '@angular/core';

@Directive({
    selector: "[gen]",
    standalone: true,
})
export class GenericsDirective<T, S> {

    @Input()
    input1: T;

    @Input()
    input2: T;

    @Input()
    input3: S;

    @Output()
    output1: EventEmitter<T>

    @Output()
    output3: EventEmitter<S>

}

@Directive({
    selector: "[gen2]",
    standalone: true,
})
export class GenericsDirective2<T> {

    @Input()
    input1: T;

    @Input()
    input2: string;

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
         [input2]="<error descr="Type string is not assignable to type boolean" textAttributesKey="ERRORS_ATTRIBUTES">'20'</error>" 
         [input3]="12" 
         (output1)="useNumber(<error descr="Argument type boolean is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)" 
         (output3)="useString(<error descr="Argument type number is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>
         
    <div gen
         [input1]="12"
         [input2]="<error descr="Type string is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">'20'</error>"
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
         (output1)="useNumber(<error descr="Argument type number[] is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>
    
    <div gen
         input1
         (output1)="useNumber($event)">
    </div>

    <div gen
         input1
         input2
         (output1)="useNumber(<error descr="Argument type string is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>

    <div gen
         <warning descr="[input1] requires value" textAttributesKey="WARNING_ATTRIBUTES">[input1]</warning>
         (output1)="useString($event)">
    </div>
    
    <div gen
         [input1]=""
         input2
         (output1)="useNumber(<error descr="Argument type string | undefined is not assignable to parameter type number  Type string is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">$event</error>) && useString(<error descr="Argument type string | undefined is not assignable to parameter type string  Type undefined is not assignable to type string" textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>

    <div gen
         input1
         [input2]=""
         (output1)="useNumber(<error descr="Argument type string | undefined is not assignable to parameter type number  Type string is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)">
    </div>
    
    <div gen gen2 
         [input1]="12" 
         [input2]="<error descr="Type number is not assignable to type never" textAttributesKey="ERRORS_ATTRIBUTES">12</error>">
    </div>
         
    <div gen gen2 
         [input1]="'foo'" 
         [input2]="<error descr="Type number is not assignable to type string" textAttributesKey="ERRORS_ATTRIBUTES">12</error>">
    </div>
    
    <div gen gen2 
         [input1]="12" 
         [input2]="<error descr="Type string is not assignable to type never" textAttributesKey="ERRORS_ATTRIBUTES">'foo'</error>">
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

    useNumber(<warning descr="Unused parameter val" textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES">val</warning>: number) {

    }

    useString(<warning descr="Unused parameter val" textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES">val</warning>: string) {

    }

}
