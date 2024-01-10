import { ElementRef } from '@angular/core';


/**
 * Type describing the allowed values for a boolean input.
 * @docs-private
 */
export declare type BooleanInput = string | boolean | null | undefined;


/** Wraps the provided value in an array, unless the provided value is an array. */
export declare function coerceArray<T>(value: T | T[]): T[];

export declare function coerceArray<T>(value: T | readonly T[]): readonly T[];

/** Coerces a data-bound value (typically a string) to a boolean. */
export declare function coerceBooleanProperty(value: any): boolean;


/** Coerces a value to a CSS pixel value. */
export declare function coerceCssPixelValue(value: any): string;

/**
 * Coerces an ElementRef or an Element into an element.
 * Useful for APIs that can accept either a ref or the native element itself.
 */
export declare function coerceElement<T>(elementOrRef: ElementRef<T> | T): T;

/** Coerces a data-bound value (typically a string) to a number. */
export declare function coerceNumberProperty(value: any): number;

export declare function coerceNumberProperty<D>(value: any, fallback: D): number | D;


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
export declare function coerceStringArray(value: any, separator?: string | RegExp): string[];

/**
 * Whether the provided value is considered a number.
 * @docs-private
 */
export declare function _isNumberValue(value: any): boolean;


/**
 * Type describing the allowed values for a number input
 * @docs-private
 */
export declare type NumberInput = string | number | null | undefined;

export { }
