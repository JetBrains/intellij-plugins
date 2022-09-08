interface FormatLengthOptions {
    c?: number;
    offset?: number;
    attachPx?: boolean;
}
export declare function formatLength<T extends number | string | null | undefined | any>(length: T, { c, offset, attachPx }?: FormatLengthOptions): T extends null ? null : T extends undefined ? undefined : T extends string | number ? string : T;
export {};
