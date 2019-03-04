export declare class OverlayBaseController<Opts, Overlay> {
    private ctrl;
    constructor(ctrl: string);
    create(opts?: Opts): Promise<Overlay>;
    dismiss(data?: any, role?: string, id?: string): Promise<void>;
    getTop(): Promise<Overlay>;
}
