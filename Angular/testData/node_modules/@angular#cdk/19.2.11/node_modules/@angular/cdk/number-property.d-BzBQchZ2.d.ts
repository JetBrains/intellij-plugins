/**
 * Type describing the allowed values for a number input
 * @docs-private
 */
type NumberInput = string | number | null | undefined;
/** Coerces a data-bound value (typically a string) to a number. */
declare function coerceNumberProperty(value: any): number;
declare function coerceNumberProperty<D>(value: any, fallback: D): number | D;
/**
 * Whether the provided value is considered a number.
 * @docs-private
 */
declare function _isNumberValue(value: any): boolean;

export { _isNumberValue, coerceNumberProperty };
export type { NumberInput };
