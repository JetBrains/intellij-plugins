// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
declare interface HTMLSelectElement {
  onchange: (( this: GlobalEventHandlers, ev: SelectChangeEvent ) => any) | null;
}

interface SelectChangeEvent extends Event {
  readonly target: SelectChangeEventTarget | null;
}

interface SelectChangeEventTarget extends EventTarget {
  value: any
}
