export { NumberInput, _isNumberValue, coerceNumberProperty } from '../number-property.d-BzBQchZ2.js';
import { ElementRef } from '@angular/core';

/**
 * Type describing the allowed values for a boolean input.
 * @docs-private
 */
type BooleanInput = string | boolean | null | undefined;
/** Coerces a data-bound value (typically a string) to a boolean. */
declare function coerceBooleanProperty(value: any): boolean;

/** Wraps the provided value in an array, unless the provided value is an array. */
declare function coerceArray<T>(value: T | T[]): T[];
declare function coerceArray<T>(value: T | readonly T[]): readonly T[];

/** Coerces a value to a CSS pixel value. */
declare function coerceCssPixelValue(value: any): string;

/**
 * Coerces an ElementRef or an Element into an element.
 * Useful for APIs that can accept either a ref or the native element itself.
 */
declare function coerceElement<T>(elementOrRef: ElementRef<T> | T): T;

/**
 * Coerces a value to an array of trimmed non-empty strings.
 * Any input that is not an array, `null` or `undefined` will be turned into a string
 * via `toString()` and subsequently split with the given separator.
 * `null` and `undefined` will result in an empty array.
 * This results in the following outcomes:
 * - `null` -&gt; `[]`
 * - `[null]` -&gt; `["null"]`
 * - `["a", "b ", " "]` -&gt; `["a", "b"]`
 * - `[1, [2, 3]]` -&gt; `["1", "2,3"]`
 * - `[{ a: 0 }]` -&gt; `["[object Object]"]`
 * - `{ a: 0 }` -&gt; `["[object", "Object]"]`
 *
 * Useful for defining CSS classes or table columns.
 * @param value the value to coerce into an array of strings
 * @param separator split-separator if value isn't an array
 */
declare function coerceStringArray(value: any, separator?: string | RegExp): string[];

export { coerceArray, coerceBooleanProperty, coerceCssPixelValue, coerceElement, coerceStringArray };
export type { BooleanInput };
