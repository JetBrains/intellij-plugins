import { Observable } from 'rxjs';

/**
 * Given either an Observable or non-Observable value, returns either the original
 * Observable, or wraps it in an Observable that emits the non-Observable value.
 */
declare function coerceObservable<T>(data: T | Observable<T>): Observable<T>;

export { coerceObservable };
