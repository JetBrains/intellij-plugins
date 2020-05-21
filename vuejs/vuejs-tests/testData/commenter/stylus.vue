<style lang="stylus">

table
    for row in 1 <selection> 2 3 4 5
        tr:nth-child({row}</selection>)
            height: 10px * row

</style>