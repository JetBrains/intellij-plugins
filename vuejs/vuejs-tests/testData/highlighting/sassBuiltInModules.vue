<style lang="sass">
@use 'sass:math'
@use 'sass:string' as *

//noinspection CssOverwrittenProperties
* 
   height: <warning descr="Cannot find variable 'pi'">$pi</warning>
   height: <error descr="Unknown function 'asin'">asin</error>(0)
   height: slice('', 0, 1)
   height: str-slice('', 0, 1)
   height: <error descr="Unknown function 'bad'">bad</error>('', 0, 1)

   height: math.$pi
   height: math.<warning descr="Cannot find variable 'bad'">$bad</warning>
   height: math.asin(0)
   height: math.<error descr="Unknown function 'bad'">bad</error>(0)
   height: <error descr="There is no module with the namespace 'string'">string</error>.<error descr="Unknown function 'slice'">slice</error>('', 0, 1)
</style>
