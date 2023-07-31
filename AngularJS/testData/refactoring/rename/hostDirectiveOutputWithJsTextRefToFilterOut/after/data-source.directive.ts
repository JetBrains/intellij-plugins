// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Directive, EventEmitter, Output } from '@angular/core';

@Directive({
  selector: '[appDataSource]',
  standalone: true,
})
export class DataSourceDirective<
  T = unknown
> {

  /**
   * Emits when the data source object has been changed.
   */
  @Output()
  readonly newOutput = new EventEmitter<void>();

  /**
   * Event emitted when datasource content has been changed.
   */
  @Output()
  readonly dataChanged = new EventEmitter<T[]>();
}
