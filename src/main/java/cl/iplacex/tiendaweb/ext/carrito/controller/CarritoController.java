package cl.iplacex.tiendaweb.ext.carrito.controller;

import cl.iplacex.tiendaweb.ext.carrito.domain.Carrito;
import cl.iplacex.tiendaweb.ext.carrito.domain.CarritoImpl;
import cl.iplacex.tiendaweb.ext.carrito.domain.LineaPedidoImpl;
import cl.iplacex.tiendaweb.ext.carrito.domain.Pedido;
import cl.iplacex.tiendaweb.ext.carrito.event.PedidoCompletadoEvent;
//import cl.iplacex.tiendaweb.service.CategoriaService; No se usa
import cl.iplacex.tiendaweb.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

//Aqui agregamos la integración (punto 3.4)
import org.springframework.jms.core.JmsTemplate;
import org.springframework.beans.factory.annotation.Value;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

@Controller
public class CarritoController {
    private final Logger logger = LoggerFactory.getLogger(CarritoController.class);
    private final ProductoService productoService;
    private final ApplicationEventPublisher applicationEventPublisher;

    //Agregado para integración JMS
    private final JmsTemplate jmsTemplate;

    // Lee "gma_web_pedidos" desde application.properties
    @Value("${cola.web.pedidos}")
    private String nombreCola; 

    public CarritoController(ProductoService productoService, ApplicationEventPublisher applicationEventPublisher, 
                            JmsTemplate jmsTemplate){ //se agrega ese parametro 
        this.productoService = productoService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.jmsTemplate = jmsTemplate; //Se agrega esta inicialización
    }

    @PostMapping("/comprar")
    String comprar(HttpSession session, Model model, @ModelAttribute Pedido pedido) {
        Carrito carrito = (Carrito) session.getAttribute("carrito");
        pedido.setItems(
                carrito.getLineasPedido().stream()
                        .map(lp -> (LineaPedidoImpl) lp)   // casteo explícito
                        .collect(Collectors.toList())
        );
        model.addAttribute("pedido", pedido);

        // Integración JMS: enviar el pedido como mensaje XML a la cola

        try {
            // Patrón Message Translator: Convertir Pedido a XML usando JAXB
            JAXBContext context = JAXBContext.newInstance(Pedido.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Convertir el pedido a XML
            StringWriter sw = new StringWriter();
            marshaller.marshal(pedido, sw);
            String pedidoXml = sw.toString();

            // Enviar el mensaje XML a la cola
            jmsTemplate.convertAndSend(nombreCola, pedidoXml);
            logger.info("INTEGRACIÓN GMA: Pedido enviado exitosamente a la cola: {}", nombreCola);
        } catch (Exception e) {
            logger.error("INTEGRACIÓN GMA - ERROR: No se pudo enviar el XML: {}", e.getMessage());
        }
        //Fin de integración JMS
        
        logger.info("Registrando venta de {} pesos, será enviada a {}", pedido.getTotal(), pedido.getDireccionDespacho().getComuna());
        applicationEventPublisher.publishEvent(new PedidoCompletadoEvent(pedido));
        return "compra";
    }

    @GetMapping("/carrito")
    String carrito(Model model) {
        var pedido = new Pedido();
        model.addAttribute("pedido", pedido);
        return "carrito";
    }

    @GetMapping("/carrito/remove")
    String remove(
            HttpSession session,
            @RequestParam String sku,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Carrito carrito = (Carrito) session.getAttribute("carrito");
        if(carrito == null) carrito = new CarritoImpl();
        carrito.quitarProducto(sku);
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/add")
    String add(
            HttpSession session,
            @RequestParam String sku,
            @RequestParam(name = "cant", defaultValue = "1") int cantidad,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Carrito carrito = (Carrito) session.getAttribute("carrito");
        if(carrito == null) carrito = new CarritoImpl();
        var productoOpt = productoService.getProductoBySku(sku);
        if( productoOpt.isPresent() ) {
            carrito.agregarProducto(productoOpt.get(), cantidad);
            session.setAttribute("carrito", carrito);
            redirectAttributes.addFlashAttribute("mensaje", "Se agregó al carro: "+productoOpt.get().getNombre());
        }
        return "redirect:/carrito";
    }
}
