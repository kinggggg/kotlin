fun foo(c : Collection<String>){
  c.filter<selection>{it; false}</selection>
}
/*
fun foo(c : Collection<String>){
    val function: () -> Boolean = {it; false}
    c.filter(function)
}
*/