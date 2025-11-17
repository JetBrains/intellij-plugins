/**
 * @experimental
 */
export type EventInstance<Type extends string, Payload> = {
    type: Type;
    payload: Payload;
};
export declare function isEventInstance(value: unknown): value is EventInstance<string, unknown>;
