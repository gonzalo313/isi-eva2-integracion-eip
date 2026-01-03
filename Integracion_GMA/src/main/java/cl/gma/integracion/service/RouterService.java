package cl.gma.integracion.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class RouterService {

    /**
     * PATRÓN: MESSAGE ROUTER / CONSUMER
     * Este método escucha automáticamente la cola 'gma_mkp_pedidos'.
     * Cada vez que tu Adapter mande algo, este método se activará al instante.
     */
    @JmsListener(destination = "${cola.marketplace.pedidos:gma_mkp_pedidos}")
    public void recibirPedidosMarketplace(String mensajeJson) {
        System.out.println("\n=================================================");
        System.out.println(">>> [ROUTER] ¡Nuevo Pedido Recibido en Logística!");
        System.out.println(">>> Contenido: " + mensajeJson);
        System.out.println(">>> Acción: Derivando a sistema de despacho...");
        System.out.println("=================================================\n");
    }
}