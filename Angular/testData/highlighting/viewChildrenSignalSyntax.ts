// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {<symbolName descr="identifiers//exported function">Component</symbolName>, <symbolName descr="classes//exported class">ElementRef</symbolName>, <symbolName descr="identifiers//exported function">viewChildren</symbolName>, <symbolName descr="identifiers//exported variable">viewChild</symbolName>} <info descr="null">from</info> '@angular/core';

<info descr="decorator">@</info><info descr="decorator">Component</info>({
  <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">app-test</symbolName>',
  <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
      <!-- ViewChild should grab only first reference from each template block -->
      <textarea #<symbolName descr="ng-variable">area</symbolName>></textarea>

      <textarea #<symbolName descr="ng-variable">areaReq</symbolName>></textarea>

      <div #<symbolName descr="ng-variable">area2</symbolName>></div>
    </inject>`
})
export class <symbolName descr="classes//exported class">TestComponent</symbolName> {
  <warning descr="Unused field area1"><symbolName descr="ng-signal">area1</symbolName></warning> = <symbolName descr="identifiers//exported variable">viewChild</symbolName>("<symbolName descr="NG.VARIABLE">area</symbolName>");
  <warning descr="Unused field area1a"><symbolName descr="ng-signal">area1a</symbolName></warning> = <symbolName descr="identifiers//exported variable">viewChild</symbolName><<symbolName descr="classes//exported class">ElementRef</symbolName>>("<symbolName descr="NG.VARIABLE">area</symbolName>");

  <warning descr="Unused field area1req"><symbolName descr="ng-signal">area1req</symbolName></warning> = <symbolName descr="identifiers//exported variable">viewChild</symbolName>.<symbolName descr="instance field">required</symbolName><<symbolName descr="classes//exported class">ElementRef</symbolName>>("<symbolName descr="NG.VARIABLE">areaReq</symbolName>");
  <warning descr="Unused field area1reqA"><symbolName descr="ng-signal">area1reqA</symbolName></warning> = <symbolName descr="identifiers//exported variable">viewChild</symbolName>.<symbolName descr="instance field">required</symbolName>("<symbolName descr="NG.VARIABLE">areaReq</symbolName>");

  <warning descr="Unused field area2"><symbolName descr="ng-signal">area2</symbolName></warning> = <symbolName descr="identifiers//exported function">viewChildren</symbolName><<symbolName descr="classes//exported class">ElementRef</symbolName>>("<symbolName descr="NG.VARIABLE">area2</symbolName>");
  <warning descr="Unused field area2a"><symbolName descr="ng-signal">area2a</symbolName></warning> = <symbolName descr="identifiers//exported function">viewChildren</symbolName>("<symbolName descr="NG.VARIABLE">area2</symbolName>");

  <warning descr="Unused field badArea1"><symbolName descr="ng-signal">badArea1</symbolName></warning> = <symbolName descr="identifiers//exported variable">viewChild</symbolName><<symbolName descr="classes//exported class">ElementRef</symbolName>>("<warning descr="Unrecognized name">badArea</warning>");
  <warning descr="Unused field badArea2"><symbolName descr="ng-signal">badArea2</symbolName></warning> = <symbolName descr="identifiers//exported function">viewChildren</symbolName><<symbolName descr="classes//exported class">ElementRef</symbolName>>("<warning descr="Unrecognized name">badArea</warning>");

  // Not supported
  <warning descr="Unused field area2req"><symbolName descr="instance field">area2req</symbolName></warning> = <symbolName descr="identifiers//exported function">viewChildren</symbolName>.<error descr="TS2339: Property 'required' does not exist on type '{ <LocatorT>(locator: string | ProviderToken<LocatorT>): Signal<readonly LocatorT[]>; <LocatorT, ReadT>(locator: string | ProviderToken<LocatorT>, opts: { ...; }): Signal<...>; }'.">required</error>("areaReq");
}
