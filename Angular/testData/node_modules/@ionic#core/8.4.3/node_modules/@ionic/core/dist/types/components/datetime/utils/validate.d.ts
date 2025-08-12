import type { DatetimePresentation, FormatOptions } from '../datetime-interface';
/**
 * If a time zone is provided in the format options, the rendered text could
 * differ from what was selected in the Datetime, which could cause
 * confusion.
 */
export declare const warnIfTimeZoneProvided: (el: HTMLElement, formatOptions?: FormatOptions) => void;
export declare const checkForPresentationFormatMismatch: (el: HTMLElement, presentation: DatetimePresentation, formatOptions?: FormatOptions) => void;
