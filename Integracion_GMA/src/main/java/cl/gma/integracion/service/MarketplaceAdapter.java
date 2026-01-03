package cl.gma.integracion.service;

import cl.gma.integracion.dto.CostoEnvioResponse;
import cl.gma.integracion.dto.PedidoMarketplace;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@EnableScheduling
public class MarketplaceAdapter {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    // Aquí inyectamos el nombre de tu cola definido en application.properties
    @Value("${cola.marketplace.pedidos}")
    private String colaDestino;

    // URL del Marketplace (Puerto 8091 según el PDF)
    private final String URL_BASE = "http://localhost:8091"; 

    // Se ejecuta cada 30 segundos para probar
    @Scheduled(fixedRate = 30000)
    public void procesarPedidos() {
        System.out.println(">>> [ADAPTER MKP] Buscando ventas del día...");

        try {
            // 1. Obtener lista de pedidos
            String urlPedidos = URL_BASE + "/orders/today";
            PedidoMarketplace[] pedidos = restTemplate.getForObject(urlPedidos, PedidoMarketplace[].class);

            if (pedidos == null || pedidos.length == 0) {
                System.out.println(">>> [ADAPTER MKP] No hay pedidos nuevos.");
                return;
            }

            // 2. Procesar cada pedido
            ObjectMapper mapper = new ObjectMapper();

            for (PedidoMarketplace pedido : pedidos) {
                
                // 3. Obtener costo de envío (Enriquecimiento)
                try {
                    String urlCosto = URL_BASE + "/orders/" + pedido.getId() + "/shipping-cost";
                    CostoEnvioResponse respuesta = restTemplate.getForObject(urlCosto, CostoEnvioResponse.class);

                    if (respuesta != null) {
                        pedido.setCostoEnvio(respuesta.getCostoEnvio());
                        System.out.println("   + Costo agregado: $" + respuesta.getCostoEnvio() + " para pedido " + pedido.getId());
                    }
                } catch (Exception e) {
                    System.err.println("   ! Error buscando costo: " + e.getMessage());
                    pedido.setCostoEnvio(0); // Valor por defecto si falla
                }

                // 4. Enviar a la cola
                String jsonFinal = mapper.writeValueAsString(pedido);
                jmsTemplate.convertAndSend(colaDestino, jsonFinal);
                
                System.out.println("   -> Enviado a cola JMS: " + colaDestino);
            }

        } catch (Exception e) {
            System.err.println(">>> [ERROR] No se pudo conectar al Marketplace. ¿Está corriendo en puerto 8091?");
        }
    }
}