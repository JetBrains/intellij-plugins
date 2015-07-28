package {
public class NoTypeGuessFromAsdoc2 {
    /**
     * @return
     */
    // aaaaaaaaaaaaaaaa
    private function foo():Array {
         return <error>aa</error>;
    }
}
}
