// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';

@Component({
   selector: 'app-root',
   template: `
      <div>
          <button disabled mat-icon-button></button>
          <app-root <error descr="Type undefined is not assignable to type string">[foo]</error>></app-root>
      </div>
  `,
  standalone: true,
  imports: [MatButtonModule]
})
export class AppComponent {

  @Input()
  protected foo!: string

}