export declare class DomController {
    read(cb: RafCallback): void;
    write(cb: RafCallback): void;
}
export declare type RafCallback = (timeStamp?: number) => void;
