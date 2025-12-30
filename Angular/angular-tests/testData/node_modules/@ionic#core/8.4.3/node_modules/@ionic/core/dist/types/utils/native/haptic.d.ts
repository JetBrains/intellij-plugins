import type { NotificationType as CapacitorNotificationType, ImpactStyle as CapacitorImpactStyle } from '@capacitor/haptics';
export declare enum ImpactStyle {
    /**
     * A collision between large, heavy user interface elements
     *
     * @since 1.0.0
     */
    Heavy = "HEAVY",
    /**
     * A collision between moderately sized user interface elements
     *
     * @since 1.0.0
     */
    Medium = "MEDIUM",
    /**
     * A collision between small, light user interface elements
     *
     * @since 1.0.0
     */
    Light = "LIGHT"
}
interface HapticImpactOptions {
    style: CapacitorImpactStyle;
}
export declare enum NotificationType {
    /**
     * A notification feedback type indicating that a task has completed successfully
     *
     * @since 1.0.0
     */
    Success = "SUCCESS",
    /**
     * A notification feedback type indicating that a task has produced a warning
     *
     * @since 1.0.0
     */
    Warning = "WARNING",
    /**
     * A notification feedback type indicating that a task has failed
     *
     * @since 1.0.0
     */
    Error = "ERROR"
}
interface HapticNotificationOptions {
    type: CapacitorNotificationType;
}
/**
 * Check to see if the Haptic Plugin is available
 * @return Returns `true` or false if the plugin is available
 */
export declare const hapticAvailable: () => boolean;
/**
 * Trigger a selection changed haptic event. Good for one-time events
 * (not for gestures)
 */
export declare const hapticSelection: () => void;
/**
 * Tell the haptic engine that a gesture for a selection change is starting.
 */
export declare const hapticSelectionStart: () => void;
/**
 * Tell the haptic engine that a selection changed during a gesture.
 */
export declare const hapticSelectionChanged: () => void;
/**
 * Tell the haptic engine we are done with a gesture. This needs to be
 * called lest resources are not properly recycled.
 */
export declare const hapticSelectionEnd: () => void;
/**
 * Use this to indicate success/failure/warning to the user.
 * options should be of the type `{ type: NotificationType.SUCCESS }` (or `WARNING`/`ERROR`)
 */
export declare const hapticNotification: (options: HapticNotificationOptions) => void;
/**
 * Use this to indicate success/failure/warning to the user.
 * options should be of the type `{ style: ImpactStyle.LIGHT }` (or `MEDIUM`/`HEAVY`)
 */
export declare const hapticImpact: (options: HapticImpactOptions) => void;
export {};
