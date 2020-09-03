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

    val analizadorDePrueba = Analizador()

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

    it("Devuelve los pedidos atentidos"){
      servidor.agregarModulo(moduloTest)
      val respuesta = servidor.realizarPedido("123.3.12.3", "http://pepito.com.ar/hola.rar", LocalDateTime.now())
      val primerPedido = servidor.primerAnalizador().primerPedido()
      primerPedido.respuesta.shouldBe(respuesta)
      primerPedido.modulo.shouldBe(moduloTest)
    }

    describe("Analizador de demoras"){
      val detectorDeDemoras = AnalizadorDemoras(250)
      val moduloTest2 = Modulo(listOf("wlk"), "bravo!", 200)
      it("Detecta las demoras"){
        servidor.realizarPedido("123.3.12.3", "http://pepito.com.ar/hola.wlk", LocalDateTime.now())
        detectorDeDemoras.cantidadRespuestasDemoradas().shouldBeEquals(0)
        servidor.realizarPedido("123.3.12.3", "http://pepito.com.ar/hola.rar", LocalDateTime.now())
        detectorDeDemoras.cantidadRespuestasDemoradas().shouldBeEquals(1)
      }
    }

    describe("Analizador de Ips Sospechosas"){
      val analizadorIps = AnalizadorDeIps(listOf<String>("123.32.3.1"))


    }
  }
})
