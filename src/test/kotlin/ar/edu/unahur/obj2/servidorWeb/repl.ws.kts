class Pedidos(val respuesta : String, val numero : Int ){

}

val pedido1 = Pedidos("Genial!",10)

val pedido2 = Pedidos("asombroso!",11)

val pedido3 = Pedidos("Genial!",10)

val x = mutableListOf<Pedidos>(pedido1,pedido2,pedido3)

val o = x.first()

print(o == pedido1)