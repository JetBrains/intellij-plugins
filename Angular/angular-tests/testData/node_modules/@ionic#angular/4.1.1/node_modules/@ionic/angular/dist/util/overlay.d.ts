export declare class OverlayBaseController<Opts, Overlay> {
    private ctrl;
    private doc;
    constructor(ctrl: string, doc: Document);
    /**
     * Creates a new overlay
     */
    create(opts?: Opts): Promise<Overlay>;
    /**
     * When `id` is not provided, it dismisses the top overlay.
     */
    dismiss(data?: any, role?: string, id?: string): Promise<void>;
    /**
     * Returns the top overlay.
     */
    getTop(): Promise<Overlay>;
}
