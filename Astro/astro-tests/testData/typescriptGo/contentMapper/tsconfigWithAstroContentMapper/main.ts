import Card from "./Card.astro"
import { double } from "./utils"

double(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type 'number'.">"42"</error>)

export default Card
