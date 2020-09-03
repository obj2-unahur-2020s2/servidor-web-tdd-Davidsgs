package ar.edu.unahur.obj2.servidorWeb

class Pedido(val modulo : Modulo?,val respuesta: Respuesta){}

class Analizador(){
    val pedidos = mutableListOf<Pedido>()

    fun primerPedido(): Pedido {
        return pedidos.first()
    }

    fun agregarPedido(pedido : Pedido) = pedidos.add(pedido)

}