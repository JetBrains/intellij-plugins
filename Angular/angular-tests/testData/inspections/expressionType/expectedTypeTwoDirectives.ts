// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, <error descr="TS2449: Class 'AmbiguousDirective' used before its declaration.">AmbiguousDirective</error>, <error descr="TS2449: Class 'AmbiguousDirectiveDuplicate' used before its declaration.">AmbiguousDirectiveDuplicate</error>],
  standalone: true,
  template: `
    <!-- todo Angular checks types one by one and reports 1-2 errors separately -->
    <span <error descr="TS2322: Type 'string' is not assignable to type 'boolean'."><error descr="TS2322: Type 'string' is not assignable to type 'number'.">[appAmbiguous]</error></error>="'hello'"></span>
    <span <error descr="TS2322: Type 'boolean' is not assignable to type 'number'.">[appAmbiguous]</error>="true"></span>
    <span <error descr="TS2322: Type 'number' is not assignable to type 'boolean'.">[appAmbiguous]</error>="1"></span>
  `,
})
export class TestComponent {
}

@Directive({
  selector: '[appAmbiguous]',
  standalone: true
})
export class AmbiguousDirective {
  @Input() appAmbiguous!: boolean;
}

@Directive({
  selector: '[appAmbiguous]',
  standalone: true
})
export class AmbiguousDirectiveDuplicate {
  @Input() appAmbiguous!: number;
}
