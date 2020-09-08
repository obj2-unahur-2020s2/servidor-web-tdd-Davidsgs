package ar.edu.unahur.obj2.servidorWeb

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime

class ServidorWebTest : DescribeSpec({
  describe("Un servidor web") {
    val servidor = ServidorWeb()
    servidor.agregarModulo(
      Modulo(listOf("txt"), "todo bien", 100)
    )
    servidor.agregarModulo(
      Modulo(listOf("jpg", "gif"), "qué linda foto", 100)
    )

    it("devuelve 501 si recibe un pedido que no es HTTP") {
      val respuesta = servidor.realizarPedido("207.46.13.5", "https://pepito.com.ar/hola.txt", LocalDateTime.now())
      respuesta.codigo.shouldBe(CodigoHttp.NOT_IMPLEMENTED)
      respuesta.body.shouldBe("")
    }

    it("devuelve 200 si algún módulo puede trabajar con el pedido") {
      val respuesta = servidor.realizarPedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now())
      respuesta.codigo.shouldBe(CodigoHttp.OK)
      respuesta.body.shouldBe("todo bien")
    }

    it("devuelve 404 si ningún módulo puede trabajar con el pedido") {
      val respuesta = servidor.realizarPedido("207.46.13.5", "http://pepito.com.ar/playa.png", LocalDateTime.now())
      respuesta.codigo.shouldBe(CodigoHttp.NOT_FOUND)
      respuesta.body.shouldBe("")
    }

    //-----------------------------------

    val analizadorDePrueba = AnalizadorDemoras(100)

    servidor.agregarAnalizador(
      analizadorDePrueba
    )

    it("No tiene analizadores"){
      servidor.quitarAnalizador(analizadorDePrueba)
      servidor.analizadores.size.shouldBe(0)
    }

    it("Tiene analizadores"){
      servidor.analizadores.size.shouldNotBe(0)
    }

    val moduloTest = Modulo(listOf("rar"), "genial!", 300)
    val moduloTest2 = Modulo(listOf("wlk"), "bravo!", 200)
    servidor.agregarModulo(moduloTest)
    servidor.agregarModulo(moduloTest2)

    it("Devuelve los pedidos atentidos"){
      servidor.agregarModulo(moduloTest)
      val respuesta = servidor.realizarPedido("123.3.12.3", "http://pepito.com.ar/hola.rar", LocalDateTime.now())
      val primerPedido = servidor.primerAnalizador().primerPedido()
      primerPedido.respuesta.shouldBe(respuesta)
      primerPedido.modulo.shouldBe(moduloTest)
    }

    describe("Analizador de demoras"){
      val detectorDeDemoras = AnalizadorDemoras(200)
      servidor.agregarAnalizador(detectorDeDemoras)
      it("Detecta las demoras"){
        servidor.realizarPedido("123.3.12.3", "http://pepito.com.ar/hola.wlk", LocalDateTime.now())
        detectorDeDemoras.cantidadDeDemoras().shouldBe(0)
        servidor.realizarPedido("123.3.12.3", "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        detectorDeDemoras.cantidadDeDemoras().shouldBe(1)
      }
    }

    describe("Analizador de Ips Sospechosas"){
      val ipSos1 = "123.32.3.1"
      val ipSos2 = "123.32.3.2"
      val ipSos3 = "123.32.3.3"
      val analizadorIps = AnalizadorDeIps(setOf(ipSos1))
      servidor.agregarAnalizador(analizadorIps)
      it("Pedidos de Ip sospechosa"){
        servidor.realizarPedido("123.3.12.3", "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        analizadorIps.cantidadDePedidos(ipSos1).shouldBe(0)
        servidor.realizarPedido( ipSos1, "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        analizadorIps.cantidadDePedidos(ipSos1).shouldBe(1)
      }

      it("Modulo más consultado"){
        val analizadorIps2 = AnalizadorDeIps(setOf(ipSos1,ipSos2))
        servidor.agregarAnalizador(analizadorIps2)
        servidor.realizarPedido(ipSos2, "http://pepito.com.ar/algo.wlk", LocalDateTime.now())
        servidor.realizarPedido(ipSos1, "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        servidor.realizarPedido(ipSos1, "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        analizadorIps2.moduloMasConsultado().shouldBe(moduloTest)
      }

      it("Ips que siguieron la ruta: http://pepito.com.ar/hola.rar"){
        val analizadorIps2 = AnalizadorDeIps(setOf(ipSos1,ipSos2,ipSos3))
        servidor.agregarAnalizador(analizadorIps2)
        servidor.realizarPedido(ipSos1, "http://pepito.com.ar/algo.wlk", LocalDateTime.now())
        servidor.realizarPedido(ipSos2, "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        servidor.realizarPedido(ipSos3, "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        analizadorIps2.ipsQueRequirieronLaRuta("http://pepito.com.ar/hola.rar").shouldBe(setOf(ipSos2,ipSos3))
      }
    }

    describe("Analizador Estadistica"){
      val analizadorEst = AnalizadorEstadistica()
      servidor.agregarAnalizador(analizadorEst)

      it("Tiempo de respuesta promedio"){
        servidor.realizarPedido("192.168.0.1", "http://pepito.com.ar/algo.wlk", LocalDateTime.now())
        servidor.realizarPedido("192.168.0.1", "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        analizadorEst.tiempoPromedio().shouldBe(250)
      }

      it("Pedidos entre 2 fechas"){
        servidor.realizarPedido("207.46.13.5", "http://pepito.com.ar/hola.rar", LocalDateTime.of(2001, 5, 15, 6, 31))
        servidor.realizarPedido("207.46.13.3", "http://pepito.com.ar/hola.rar", LocalDateTime.of(2017, 3, 21, 5, 31))
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.rar", LocalDateTime.of(2020, 1, 23, 1, 35))
        val fecha1 = LocalDateTime.of(2016, 3, 21, 5, 31)
        val fecha2 = LocalDateTime.of(2021, 1, 23, 1, 35)
        analizadorEst.cantidadDePedidosEntre(fecha1,fecha2).shouldBe(2)
      }

      it("Respuestas cuyo body incluye un determinado String"){
        val unModulo = Modulo(listOf("file"), "genial, todo va bien!", 100)
        servidor.agregarModulo(unModulo)
        servidor.realizarPedido("192.168.0.1", "http://pepito.com.ar/algo.rar", LocalDateTime.now())
        servidor.realizarPedido("192.168.0.2", "http://pepito.com.ar/algo.file", LocalDateTime.now())
        servidor.realizarPedido("192.168.0.4", "http://pepito.com.ar/algo.rar", LocalDateTime.now())
        analizadorEst.cantidadDeRespuestaCon("genial").shouldBe(3)
      }

      it("Porcentaje de respuestas exitosas"){
        servidor.realizarPedido("192.168.0.1", "http://pepito.com.ar/algo.rar", LocalDateTime.now())
        servidor.realizarPedido("192.168.0.1", "http://pepito.com.ar/algo.noExiste", LocalDateTime.now())
        analizadorEst.porcentajeExitoso().shouldBe(50)
      }



    }
  }
})
