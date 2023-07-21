// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';

@Component({
   selector: 'app-root',
   exportAs: "<usage>ap<caret>p</usage>",
   template: `
    <app-root ref-a="<usage>app</usage>"></app-root>
    <div ref-b="app"></div>
   `,
 })
export class BoldDirective {
}
