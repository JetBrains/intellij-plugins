// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, AmbiguousDirective, AmbiguousDirectiveDuplicate],
  standalone: true,
  template: `
    <!-- todo Angular checks types one by one and reports 1-2 errors separately -->
    <span [appAmbiguous]="<error descr="Type  \"hello\"  is not assignable to type  never ">'hello'</error>"></span>
    <span [appAmbiguous]="<error descr="Type  true  is not assignable to type  never ">true</error>"></span>
    <span [appAmbiguous]="<error descr="Type  1  is not assignable to type  never ">1</error>"></span>
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
