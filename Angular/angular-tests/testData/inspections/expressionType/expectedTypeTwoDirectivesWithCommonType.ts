// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, <error descr="TS2449: Class 'AmbiguousDirective' used before its declaration.">AmbiguousDirective</error>, <error descr="TS2449: Class 'AmbiguousDirectiveDuplicate' used before its declaration.">AmbiguousDirectiveDuplicate</error>],
  standalone: true,
  template: `
    <span <error descr="TS2322: Type '1' is not assignable to type '2'.">[appAmbiguous]</error>="1"></span>
    <span [appAmbiguous]="2"></span>
  `,
})
export class TestComponent {
}

@Directive({
  selector: '[appAmbiguous]',
  standalone: true
})
export class AmbiguousDirective {
  @Input() appAmbiguous!: 2;
}

@Directive({
  selector: '[appAmbiguous]',
  standalone: true
})
export class AmbiguousDirectiveDuplicate {
  @Input() appAmbiguous!: number;
}
