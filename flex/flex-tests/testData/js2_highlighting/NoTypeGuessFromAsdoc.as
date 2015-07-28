package {
public class NoTypeGuessFromAsdoc {
    /**
     * aaaaaaaaaaa
     *
     * asd
     * ad
     * a
     * da
     * d
     * @return
     */
    private function foo():Array {
         return <error>aa</error>;
    }
}
}
