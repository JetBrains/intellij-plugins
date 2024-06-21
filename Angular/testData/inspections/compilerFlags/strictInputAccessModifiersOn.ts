// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Input, Directive} from '@angular/core';

@Directive({standalone: true, selector: '[foo]'})
export class TestDir {
  @Input()
  publicField!: string;
  @Input()
  protected protectedField!: string;
  @Input()
  private <weak_warning descr="TS6133: 'privateField' is declared but its value is never read.">privateField</weak_warning>!: string;
  @Input()
  readonly readonlyField!: string;
}

@Component({
             selector: 'blah',
             template: `<div foo
                    [publicField]="value"
                    <error descr="Cannot assign to input readonlyField because it is a read-only property">[readonlyField]</error>="value" 
                    <error descr="Field protectedField is protected and it is only accessible within class TestDir and its subclasses">[protectedField]</error>="value"
                    <error descr="Field privateField is private and it is only accessible within class TestDir">[privateField]</error>="value"
               ></div>`,
             standalone: true,
             imports: [
               TestDir
             ],
           })
export class FooCmp {
  value = "value";
}