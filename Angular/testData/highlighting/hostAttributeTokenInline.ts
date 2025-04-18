// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Attribute, Directive, Component, HostAttributeToken, inject} from '@angular/core';

@Directive({
  selector: '[appValidateEqual]',
  standalone: true
})
export class EqualValidator {
  <warning descr="Unused field validateEqual">validateEqual</warning> = inject(new HostAttributeToken('appValidateEqual'));
  <warning descr="Unused field reverse">reverse</warning> = inject(new HostAttributeToken('reverse'), {optional: true});
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
  template: `
    <div></div>
    <div appValidateEqual="1"></div>
    <div appValidateEqual="1" reverse="12"></div>
    <div <warning descr="Attribute reverse is not allowed here">reverse</warning>="12"></div>
    
    <app-sub-component dataTestIdContexts="my-test-id" testAttrs="foo" />
    <<warning descr="Element app-sub-component doesn't have required attribute dataTestIdContexts">app-sub-component</warning> />
    <<warning descr="Element app-sub-component doesn't have required attribute dataTestIdContexts">app-sub-component</warning> <warning descr="Attribute dataTestIdContext is not allowed here">dataTestIdContext</warning>="my-test-id" <warning descr="Attribute testAttr is not allowed here">testAttr</warning>="foo"/>
  `,
  imports: [SubComponent],
})
export class AppComponent {}

