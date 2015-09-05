package {
import starling.display.Quad

[Event(type="TouchEv")]
public class StarlingEvent {
    public function StarlingEvent {
        new Quad().addEventListener();
    }
}
}

package starling.events{
    public class Event{
        /** Event type for a display object that is added to a parent. */
        public static const ADDED:String = "added";
        /** Event type for a display object that is added to the stage */
        public static const ADDED_TO_STAGE:String = "addedToStage";
        /** Event type for a display object that is entering a new frame. */
        public static const ENTER_FRAME:String = "enterFrame";
        /** Event type for a display object that is removed from its parent. */
        public static const REMOVED:String = "removed";
        /** Event type for a display object that is removed from the stage. */
        public static const REMOVED_FROM_STAGE:String = "removedFromStage";
        /** Event type for a triggered button. */
        public static const TRIGGERED:String = "triggered";
        /** Event type for a display object that is being flattened. */
        public static const FLATTEN:String = "flatten";
        /** Event type for a resized Flash Player. */
        public static const RESIZE:String = "resize";
        /** Event type that may be used whenever something finishes. */
        public static const COMPLETE:String = "complete";
        /** Event type for a (re)created stage3D rendering context. */
        public static const CONTEXT3D_CREATE:String = "context3DCreate";
        /** Event type that indicates that the root DisplayObject has been created. */
        public static const ROOT_CREATED:String = "rootCreated";
        /** Event type for an animated object that requests to be removed from the juggler. */
        public static const REMOVE_FROM_JUGGLER:String = "removeFromJuggler";

        /** An event type to be utilized in custom events. Not used by Starling right now. */
        public static const CHANGE:String = "change";
        /** An event type to be utilized in custom events. Not used by Starling right now. */
        public static const CANCEL:String = "cancel";
        /** An event type to be utilized in custom events. Not used by Starling right now. */
        public static const SCROLL:String = "scroll";
        /** An event type to be utilized in custom events. Not used by Starling right now. */
        public static const OPEN:String = "open";
        /** An event type to be utilized in custom events. Not used by Starling right now. */
        public static const CLOSE:String = "close";
        /** An event type to be utilized in custom events. Not used by Starling right now. */
        public static const SELECT:String = "select";
    }

    public class TouchEvent extends Event {
        public static const TOUCH:String = "touch";
    }
}

package starling.display {
[Event(name="added", type="starling.events.Event")]
/** Dispatched when an object is connected to the stage (directly or indirectly). */
[Event(name="addedToStage", type="starling.events.Event")]
/** Dispatched when an object is removed from its parent. */
[Event(name="removed", type="starling.events.Event")]
/** Dispatched when an object is removed from the stage and won't be rendered any longer. */
[Event(name="removedFromStage", type="starling.events.Event")]
/** Dispatched when an object is touched. Bubbles. */
[Event(name="touch", type="starling.events.TouchEvent")]
public class DisplayObject{
    public function addEventListener(type:String, listener:Function):void{}
}

public class Quad extends DisplayObject{}
}