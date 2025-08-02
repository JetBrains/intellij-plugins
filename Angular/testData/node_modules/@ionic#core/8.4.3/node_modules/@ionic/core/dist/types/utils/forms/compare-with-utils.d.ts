type CompareFn = (currentValue: any, compareValue: any) => boolean;
/**
 * Uses the compareWith param to compare two values to determine if they are equal.
 *
 * @param currentValue The current value of the control.
 * @param compareValue The value to compare against.
 * @param compareWith The function or property name to use to compare values.
 */
export declare const compareOptions: (currentValue: any, compareValue: any, compareWith?: string | CompareFn | null) => boolean;
/**
 * Compares a value against the current value(s) to determine if it is selected.
 *
 * @param currentValue The current value of the control.
 * @param compareValue The value to compare against.
 * @param compareWith The function or property name to use to compare values.
 */
export declare const isOptionSelected: (currentValue: any[] | any, compareValue: any, compareWith?: string | CompareFn | null) => boolean;
export {};
