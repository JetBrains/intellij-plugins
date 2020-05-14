/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { BooleanInput } from '@angular/cdk/coercion';
import { ElementRef } from '@angular/core';
import { CanDisable, CanDisableCtor } from '@angular/material/core';
/** @docs-private */
import * as ɵngcc0 from '@angular/core';
declare class MatTabLabelWrapperBase {
}
declare const _MatTabLabelWrapperMixinBase: CanDisableCtor & typeof MatTabLabelWrapperBase;
/**
 * Used in the `mat-tab-group` view to display tab labels.
 * @docs-private
 */
export declare class MatTabLabelWrapper extends _MatTabLabelWrapperMixinBase implements CanDisable {
    elementRef: ElementRef;
    constructor(elementRef: ElementRef);
    /** Sets focus on the wrapper element */
    focus(): void;
    getOffsetLeft(): number;
    getOffsetWidth(): number;
    static ngAcceptInputType_disabled: BooleanInput;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<MatTabLabelWrapper, never>;
    static ɵdir: ɵngcc0.ɵɵDirectiveDefWithMeta<MatTabLabelWrapper, "[matTabLabelWrapper]", never, { "disabled": "disabled"; }, {}, never>;
}
export {};

//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoidGFiLWxhYmVsLXdyYXBwZXIuZC50cyIsInNvdXJjZXMiOlsidGFiLWxhYmVsLXdyYXBwZXIuZC50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FBQ0E7QUFDQSIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuaW1wb3J0IHsgQm9vbGVhbklucHV0IH0gZnJvbSAnQGFuZ3VsYXIvY2RrL2NvZXJjaW9uJztcbmltcG9ydCB7IEVsZW1lbnRSZWYgfSBmcm9tICdAYW5ndWxhci9jb3JlJztcbmltcG9ydCB7IENhbkRpc2FibGUsIENhbkRpc2FibGVDdG9yIH0gZnJvbSAnQGFuZ3VsYXIvbWF0ZXJpYWwvY29yZSc7XG4vKiogQGRvY3MtcHJpdmF0ZSAqL1xuZGVjbGFyZSBjbGFzcyBNYXRUYWJMYWJlbFdyYXBwZXJCYXNlIHtcbn1cbmRlY2xhcmUgY29uc3QgX01hdFRhYkxhYmVsV3JhcHBlck1peGluQmFzZTogQ2FuRGlzYWJsZUN0b3IgJiB0eXBlb2YgTWF0VGFiTGFiZWxXcmFwcGVyQmFzZTtcbi8qKlxuICogVXNlZCBpbiB0aGUgYG1hdC10YWItZ3JvdXBgIHZpZXcgdG8gZGlzcGxheSB0YWIgbGFiZWxzLlxuICogQGRvY3MtcHJpdmF0ZVxuICovXG5leHBvcnQgZGVjbGFyZSBjbGFzcyBNYXRUYWJMYWJlbFdyYXBwZXIgZXh0ZW5kcyBfTWF0VGFiTGFiZWxXcmFwcGVyTWl4aW5CYXNlIGltcGxlbWVudHMgQ2FuRGlzYWJsZSB7XG4gICAgZWxlbWVudFJlZjogRWxlbWVudFJlZjtcbiAgICBjb25zdHJ1Y3RvcihlbGVtZW50UmVmOiBFbGVtZW50UmVmKTtcbiAgICAvKiogU2V0cyBmb2N1cyBvbiB0aGUgd3JhcHBlciBlbGVtZW50ICovXG4gICAgZm9jdXMoKTogdm9pZDtcbiAgICBnZXRPZmZzZXRMZWZ0KCk6IG51bWJlcjtcbiAgICBnZXRPZmZzZXRXaWR0aCgpOiBudW1iZXI7XG4gICAgc3RhdGljIG5nQWNjZXB0SW5wdXRUeXBlX2Rpc2FibGVkOiBCb29sZWFuSW5wdXQ7XG59XG5leHBvcnQge307XG4iXX0=