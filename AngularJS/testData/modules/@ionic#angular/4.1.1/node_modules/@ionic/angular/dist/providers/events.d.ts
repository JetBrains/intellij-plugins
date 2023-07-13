export declare type EventHandler = (...args: any[]) => any;
export declare class Events {
    private c;
    /**
     * Subscribe to an event topic. Events that get posted to that topic will trigger the provided handler.
     *
     * @param topic the topic to subscribe to
     * @param handler the event handler
     */
    subscribe(topic: string, ...handlers: EventHandler[]): void;
    /**
     * Unsubscribe from the given topic. Your handler will no longer receive events published to this topic.
     *
     * @param topic the topic to unsubscribe from
     * @param handler the event handler
     *
     * @return true if a handler was removed
     */
    unsubscribe(topic: string, handler?: EventHandler): boolean;
    /**
     * Publish an event to the given topic.
     *
     * @param topic the topic to publish to
     * @param eventData the data to send as the event
     */
    publish(topic: string, ...args: any[]): any[] | null;
}
