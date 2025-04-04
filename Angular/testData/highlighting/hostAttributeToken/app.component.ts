// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Attribute, Directive, Component, HostAttributeToken, inject} from '@angular/core';

@Directive({
  selector: '[appValidateEqual]',
  standalone: true
})
export class EqualValidator {
  validateEqual = inject(new HostAttributeToken('appValidateEqual'));
  reverse = inject(new HostAttributeToken('reverse'), {optional: true});
}

@Component({
  selector: 'app-sub-component',
  standalone: true,
  template: `<div [attr.data-testId]="dataTestIdContexts">foo</div> {{foo}} - {{dataTestIdContexts}}`,
})
export class SubComponent {
  constructor( @Attribute("testAttrs") protected foo: string) {
  }

  dataTestIdContexts = inject(new HostAttributeToken('dataTestIdContexts'));
}

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  imports: [SubComponent],
})
export class AppComponent {}

