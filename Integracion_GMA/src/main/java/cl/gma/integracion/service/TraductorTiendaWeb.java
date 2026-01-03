package cl.gma.integracion.service;

import cl.gma.integracion.dto.web.PedidoWeb;
import cl.gma.integracion.modelo.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class TraductorTiendaWeb {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ClienteFacturacionSoap clienteFacturacion;

    @Value("${cola.pedidos.central}")
    private String colaSalida;

    // Usamos el nombre directo de la cola para evitar errores de properties
    @JmsListener(destination = "gma_web_pedidos")
    public void traducirXml(String mensajeXml) {
        System.out.println("=================================================");
        System.out.println(">>> [TRADUCTOR WEB] ¡MENSAJE CAPTURADO!");
        
        try {
            XmlMapper xmlMapper = new XmlMapper();
            ObjectMapper jsonMapper = new ObjectMapper();

            // 1. Convertir XML -> Objeto Java
            PedidoWeb pedidoWeb = xmlMapper.readValue(mensajeXml, PedidoWeb.class);
            System.out.println(">>> [TRADUCTOR WEB] XML parseado correctamente. Mapeando...");

            // 2. Mapear a Modelo Canónico
            MensajeCanonico canonico = new MensajeCanonico();
            
            // Metadata
            MensajeCanonico.Metadata meta = new MensajeCanonico.Metadata();
            meta.traceId = UUID.randomUUID().toString();
            meta.processedAt = Instant.now().toString();
            canonico.setCanonicalMetadata(meta);

            // Order
            PedidoCanonico order = new PedidoCanonico();
            canonico.setOrder(order);

            // Header
            Encabezado header = new Encabezado();
            header.originalOrderId = UUID.randomUUID().toString();
            header.originSource = "TIENDA_WEB";
            header.status = "PLACED";
            header.currency = "CLP";
            header.orderDate = pedidoWeb.fecha;
            order.setHeader(header);

            // Customer
            Cliente cliente = new Cliente();
            if (pedidoWeb.comprador != null) {
                cliente.firstName = pedidoWeb.comprador.nombre; // A veces viene nombre completo, lo dejamos así
                cliente.taxId = pedidoWeb.comprador.rut;
                cliente.phone = pedidoWeb.comprador.telefono;
                cliente.customerId = pedidoWeb.comprador.rut;
            }
            order.setCustomer(cliente);

            // Address
            Direccion direccion = new Direccion();
            if (pedidoWeb.direccionDespacho != null) {
                // TiendaWeb a veces manda "calleYNumero" en vez de "calle"
                String calle = pedidoWeb.direccionDespacho.calle;
                if (calle == null) calle = pedidoWeb.direccionDespacho.calleYNumero;
                
                direccion.street = calle;
                direccion.city = pedidoWeb.direccionDespacho.comuna;
                direccion.country = "CL";
            }
            order.setShippingAddress(direccion);
            order.setBillingAddress(direccion);

            // Items (AQUÍ ESTABA EL ERROR, AHORA ACCEDEMOS A .producto)
            order.setItems(new ArrayList<>());
            int subtotal = 0;

            if (pedidoWeb.items != null) {
                for (PedidoWeb.ItemWeb itemWeb : pedidoWeb.items) {
                    ItemCanonico itemCan = new ItemCanonico();
                    
                    // Validamos que el producto no venga nulo
                    if (itemWeb.producto != null) {
                        itemCan.setSku(itemWeb.producto.sku);
                        itemCan.setName(itemWeb.producto.nombre);
                        
                        // Usamos precioLista, si es null ponemos 0 para no caer
                        int precio = itemWeb.producto.precio != null ? itemWeb.producto.precio : 0;
                        itemCan.setUnitPrice(precio);
                    } else {
                        itemCan.setName("Producto Desconocido");
                        itemCan.setUnitPrice(0);
                    }

                    itemCan.setQuantity(itemWeb.cantidad);
                    
                    int totalLinea = itemCan.getQuantity() * itemCan.getUnitPrice();
                    itemCan.setTotalPrice(totalLinea);
                    
                    subtotal += totalLinea;
                    order.getItems().add(itemCan);
                }
            }

            // Finanzas
            Finanzas finanzas = new Finanzas();
            finanzas.subtotal = subtotal;
            finanzas.shippingCost = 0;
            finanzas.grandTotal = subtotal;
            finanzas.totalTax = (int)(subtotal * 0.19);
            order.setFinancials(finanzas);

            // 3. Enviar
            String jsonFinal = jsonMapper.writeValueAsString(canonico);
            jmsTemplate.convertAndSend(colaSalida, jsonFinal);

            System.out.println(">>> [TRADUCTOR WEB] ¡Éxito! Enviado a central.");
            System.out.println("    JSON Final: " + jsonFinal);

            // 4. Llamar a facturación SOAP
            String nombreCliente = pedidoWeb.comprador != null ? pedidoWeb.comprador.nombre : "Cliente Desconocido";
            String rutCliente = pedidoWeb.comprador != null ? pedidoWeb.comprador.rut : "0-0";
            long totalVenta = subtotal; 

            // Esta línea ejecuta la llamada al ServidorSOAP iniciado vía app.bat
            clienteFacturacion.llamarFacturacion(nombreCliente, rutCliente, totalVenta);
            
        } catch (Exception e) {
            System.err.println(">>> [ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}