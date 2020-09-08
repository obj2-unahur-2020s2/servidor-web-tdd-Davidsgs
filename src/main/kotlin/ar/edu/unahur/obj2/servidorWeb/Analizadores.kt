package ar.edu.unahur.obj2.servidorWeb

import java.time.LocalDateTime
import java.util.*

class Pedido(val modulo : Modulo?,val respuesta: Respuesta, val ip : String, val url : String, val fecha : LocalDateTime){}

abstract class Analizador(){
    val pedidos = mutableListOf<Pedido>()

    fun primerPedido(): Pedido {
        return pedidos.first()
    }

    open fun agregarPedido(pedido : Pedido) : Unit {
        pedidos.add(pedido)
    }

    fun modulosConsultados() : Set<Modulo?>{
        return pedidos.map{it.modulo}.toSet()
    }

    fun respuestas() = pedidos.map{it.respuesta}


}

class AnalizadorDemoras(val tiempoMinimo: Int) : Analizador(){
    fun cantidadDeDemoras() : Int {
        return pedidos.filter{p -> p.respuesta.tiempo > tiempoMinimo}.size
    }
}

class AnalizadorDeIps(val ipsSospechosas : Set<String>) : Analizador() {

    override fun agregarPedido(pedido : Pedido) : Unit {
        if(ipsSospechosas.contains(pedido.ip)){
            pedidos.add(pedido)
        }
    }

    fun cantidadDePedidos(ipSospechosa : String) : Int{
        return pedidos.filter{ p -> p.ip == ipSospechosa}.count()
    }

    fun moduloMasConsultado() : Modulo?{
        return pedidos.groupBy{it.modulo}.maxBy{it.component2().size}?.component1()
    }

    fun ipsQueRequirieronLaRuta(ruta : String) : Set<String>{
        return pedidos.filter{ it.url == ruta}.map{it.ip}.toSet()
    }

}

class AnalizadorEstadistica() : Analizador(){

    fun tiempoPromedio(): Int {
        return respuestas().sumBy{r -> r.tiempo} / respuestas().count()
    }

    fun cantidadDePedidosEntre(fecha1: LocalDateTime, fecha2: LocalDateTime) : Int{
        return pedidos.count { fecha1.isBefore(it.fecha) && fecha2.isAfter(it.fecha) }
    }

    fun cantidadDeRespuestaCon(body: String): Int {
        return respuestas().count { it.body.contains(body) }
    }

    fun porcentajeExitoso() = respuestas().count { it.codigo == CodigoHttp.OK } * 100 / respuestas().count()


}


