package cl.gma.integracion.service;

import cl.gma.integracion.dto.PedidoMarketplace;
import cl.gma.integracion.modelo.*; 
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Service
public class TraductorMarketplace {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${cola.pedidos.central}")
    private String colaSalida;

    @JmsListener(destination = "${cola.marketplace.pedidos}")
    public void traducir(String mensajeJson) {
        System.out.println(">>> [TRADUCTOR MKP] Mensaje recibido...");
        
        ObjectMapper mapper = new ObjectMapper();

        try {
            PedidoMarketplace pedidoMkp = mapper.readValue(mensajeJson, PedidoMarketplace.class);

            // Usamos MensajeCanonico como raíz
            MensajeCanonico canonico = new MensajeCanonico();
            
            // Metadata
            MensajeCanonico.Metadata meta = new MensajeCanonico.Metadata();
            meta.traceId = UUID.randomUUID().toString();
            meta.processedAt = Instant.now().toString();
            canonico.setCanonicalMetadata(meta);

            // Order (PedidoCanonico)
            PedidoCanonico order = new PedidoCanonico();
            canonico.setOrder(order);
            
            // --- Header ---
            Encabezado header = new Encabezado();
            header.originalOrderId = pedidoMkp.getId();
            header.originSource = "MARKETPLACE";
            header.status = "PLACED";
            header.currency = "CLP";
            header.orderDate = pedidoMkp.getFecha();
            order.setHeader(header);

            // --- Cliente ---
            Cliente cliente = new Cliente();
            Map<String, Object> clienteMkp = pedidoMkp.getCliente();
            if (clienteMkp != null) {
                cliente.firstName = (String) clienteMkp.get("nombre");
                cliente.lastName = (String) clienteMkp.get("apellido");
                cliente.taxId = (String) clienteMkp.get("rut");
                cliente.customerId = (String) clienteMkp.get("rut");
            }
            order.setCustomer(cliente);

            // --- Direccion (Shipping) ---
            Direccion direccion = new Direccion();
            Map<String, Object> dirMkp = pedidoMkp.getDireccion();
            if (dirMkp != null) {
                direccion.street = (String) dirMkp.get("calle");
                direccion.number = (String) dirMkp.get("numero");
                direccion.city = (String) dirMkp.get("comuna");
                direccion.state = (String) dirMkp.get("ciudad");
                direccion.country = "CL";
            }
            order.setShippingAddress(direccion);

            // --- Items ---
            // CORRECCIÓN AQUÍ: Usamos Setters en lugar de acceso directo
            order.setItems(new ArrayList<>());
            int subtotalCalculado = 0;

            if (pedidoMkp.getItems() != null) {
                for (Map<String, Object> itemMkp : pedidoMkp.getItems()) {
                    ItemCanonico itemCan = new ItemCanonico();
                    
                    // Usamos setters (setName, setQuantity)
                    itemCan.setName((String) itemMkp.get("producto"));
                    itemCan.setQuantity((Integer) itemMkp.get("cantidad"));
                    
                    Number precioNum = (Number) itemMkp.get("precioUnitario");
                    itemCan.setUnitPrice(precioNum.intValue());
                    
                    // Calculamos total y usamos setter
                    int totalLinea = itemCan.getQuantity() * itemCan.getUnitPrice();
                    itemCan.setTotalPrice(totalLinea);
                    
                    // Generamos SKU y usamos setter
                    itemCan.setSku("GEN-" + Math.abs(itemCan.getName().hashCode()));
                    
                    subtotalCalculado += totalLinea;
                    order.getItems().add(itemCan);
                }
            }

            // --- Finanzas ---
            Finanzas finanzas = new Finanzas();
            finanzas.subtotal = subtotalCalculado;
            finanzas.shippingCost = pedidoMkp.getCostoEnvio();
            finanzas.grandTotal = subtotalCalculado + pedidoMkp.getCostoEnvio();
            finanzas.totalTax = (int)(subtotalCalculado * 0.19);
            order.setFinancials(finanzas);

            // Enviar
            String jsonCanonico = mapper.writeValueAsString(canonico);
            jmsTemplate.convertAndSend(colaSalida, jsonCanonico);

            System.out.println(">>> [TRADUCTOR MKP] ¡Éxito! Enviado a central.");
            System.out.println("    JSON: " + jsonCanonico);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}