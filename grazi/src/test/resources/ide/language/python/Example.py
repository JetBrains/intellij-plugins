"""
    Test description of file

    I <warning>has</warning> <warning>many error</warning> in this <warning>sentnce</warning>
"""

dictionary = dict()
dictionary['kei 1'] = '<warning>this string have</warning> <warning>a</warning> error'
dictionary["key 2"] = "<warning>the a</warning> double <warning>quoutes</warning> error"


def area_by_shoelace(x, y):
    """<warning>Assumes points</warning> go around the polygon in one <warning>drection</warning>"""
    return abs(sum(i * j for i, j in zip(x, y[1:])) + x[-1] * y[0]
               - sum(i * j for i, j in zip(x[1:], y)) - x[0] * y[-1]) / 2
