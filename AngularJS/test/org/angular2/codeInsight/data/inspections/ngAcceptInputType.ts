// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Input, Component} from '@angular/core';

@Component(<error descr="SubmitButton doesn't have a template">{}</error>)
class SubmitButton {
  private _disabled: boolean;

  @Input()
  get disabled(): boolean {
    return this._disabled;
  }

  set disabled(value: boolean) {
    this._disabled = (value === '') || value;
  }

  @Input()
  foo: string

  static ngAcceptInputType_disabled: boolean|'';

  <warning descr="Unused field ngAcceptInputType_foo">ngAcceptInputType_foo</warning>: boolean|'';

  static <warning descr="Unused field ngAcceptInputType_bar">ngAcceptInputType_bar</warning>: boolean|'';
}