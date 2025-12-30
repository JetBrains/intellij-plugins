// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
// import * as i0 from "@angular/core";
import { ErrorHandler, OnDestroy, OnInit, TemplateRef, ViewContainerRef, Directive, Input } from '@angular/core';
import { Observable } from 'rxjs';

declare type Primitive = string | number | bigint | boolean | symbol | null | undefined;
declare type ObservableOrPromise<T> = Observable<T> | PromiseLike<T>;
declare type ObservableDictionary<PO> = Required<{
  [Key in keyof PO]: Observable<unknown>;
}>;
export declare type PotentialObservableResult<PO, ExtendedResult = never> = PO extends ObservableOrPromise<infer Result> ? Result | ExtendedResult : PO extends Primitive ? PO : keyof PO extends never ? PO : PO extends ObservableDictionary<PO> ? {
  [Key in keyof PO]: PO[Key] extends Observable<infer Value> ? Value : never;
} | ExtendedResult : PO;

declare type LetViewContextValue<PO> = PotentialObservableResult<PO>;

export interface LetViewContext<PO> {
  /**
   * using `$implicit` to enable `let` syntax: `*ngrxLet="obs$; let o"`
   */
  $implicit: LetViewContextValue<PO>;
  /**
   * using `ngrxLet` to enable `as` syntax: `*ngrxLet="obs$ as o"`
   */
  ngrxLet: LetViewContextValue<PO>;
  /**
   * `*ngrxLet="obs$; let e = error"` or `*ngrxLet="obs$; error as e"`
   */
  error: any;
  /**
   * `*ngrxLet="obs$; let c = complete"` or `*ngrxLet="obs$; complete as c"`
   */
  complete: boolean;
}

/**
 * Ripped from @ngrx/component@15.2.1
 */
@Directive({
  selector: '[ngrxLet]',
  standalone: true,
})
export declare class LetDirective<PO> implements OnInit, OnDestroy {
  @Input() set ngrxLet(potentialObservable: PO);
  @Input("ngrxLetSuspenseTpl") suspenseTemplateRef?: TemplateRef<unknown>;
  constructor(mainTemplateRef: TemplateRef<LetViewContext<PO | undefined>>, viewContainerRef: ViewContainerRef, errorHandler: ErrorHandler, renderScheduler: unknown);
  static ngTemplateContextGuard<PO>(dir: LetDirective<PO>, ctx: unknown): ctx is LetViewContext<PO>;
  ngOnInit(): void;
  ngOnDestroy(): void;
}
