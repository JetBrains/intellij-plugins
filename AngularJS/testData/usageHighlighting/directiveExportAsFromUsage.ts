// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';

@Component({
   selector: 'app-root',
   exportAs: "<usage>app</usage>",
   template: `
    <app-root ref-a="<usage>a<caret>pp</usage>"></app-root>
    <div ref-b="app"></div>
   `,
 })
export class BoldDirective {
}
