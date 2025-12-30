import { EventInstance } from './event-instance';
/**
 * @experimental
 */
export type EventCreator<Type extends string, Payload> = ((payload: Payload) => EventInstance<Type, Payload>) & {
    type: Type;
};
export declare function event<Type extends string>(type: Type): EventCreator<Type, void>;
export declare function event<Type extends string, Payload>(type: Type, payload: Payload): EventCreator<Type, Payload>;
