// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component, forwardRef} from "@angular/core";

@Component({
  standalone: true,
  imports: [forwardRef(() => ParentComponent)],
  selector: 'app-child-forward-ref',
  template: `
    <app-parent></app-parent>`,
})
export class ChildComponentForwardRef {
}

@Component({
  standalone: true,
  imports: [forwardRef(() => ParentComponent)],
  selector: 'app-child',
  template: `
    <app-parent></app-parent>`,
})
export class ChildComponent {
}

@Component({
  standalone: true,
  imports: [
    ChildComponentForwardRef,
    ChildComponent
  ],
  selector: 'app-parent',
  template: `
    <app-child></app-child>
    <app-child-forward-ref></app-child-forward-ref>`,
})
export class ParentComponent {
}