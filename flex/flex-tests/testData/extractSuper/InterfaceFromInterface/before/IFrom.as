package {
public interface IFrom extends IMoved, INotMoved {

    function movedMethod();

    function notMovedMethod();

    function get movedProp();

    function set notMovedProp();
}
}