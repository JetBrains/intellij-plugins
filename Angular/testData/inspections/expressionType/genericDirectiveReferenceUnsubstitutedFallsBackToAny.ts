// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, ExportedDirective],
  standalone: true,
  template: `
    <div [appExported]="true" #hello="exported"></div>
    {{expectNumber(hello.additionalField.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>)}}
  `,
})
export class TestComponent {
  expectNumber(num: number): number {
    return num;
  }
}

@Directive({
  selector: '[appExported]',
  standalone: true,
  exportAs: "exported",
})
export class ExportedDirective<T, U> {
  @Input() appExported!: T;
  additionalField!: U;
}
