import {
  Directive,
  ErrorHandler,
  Input,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewContainerRef,
} from '@angular/core';
import { Observable } from "rxjs";

 type ObservableOrPromise<T> = Observable<T> | PromiseLike<T>;

type LetViewContextValue<PO> = PO extends ObservableOrPromise<infer V> ? V : PO;

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
   * `*ngrxLet="obs$; let e = $error"` or `*ngrxLet="obs$; $error as e"`
   */
  $error: any;
  /**
   * `*ngrxLet="obs$; let c = $complete"` or `*ngrxLet="obs$; $complete as c"`
   */
  $complete: boolean;
  /**
   * `*ngrxLet="obs$; let s = $suspense"` or `*ngrxLet="obs$; $suspense as s"`
   */
  $suspense: boolean;
}

/**
 * Very simplified version of the original *ngrxLet directive
 */
@Directive({
  selector: '[ngrxLet]',
  standalone: true,
})
export class LetDirective<PO> implements OnInit, OnDestroy {
  private readonly viewContext: LetViewContext<PO | undefined> = {
    $implicit: undefined,
    ngrxLet: undefined,
    $error: undefined,
    $complete: false,
    $suspense: true,
  };

  @Input()
  set ngrxLet(potentialObservable: PO) {
  }

  constructor(
    private readonly mainTemplateRef: TemplateRef<LetViewContext<PO>>,
    private readonly viewContainerRef: ViewContainerRef,
    private readonly errorHandler: ErrorHandler,
  ) {}

  static ngTemplateContextGuard<PO>(dir: LetDirective<PO>, ctx: unknown): ctx is LetViewContext<PO> {
    return true;
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
  }

}
