package cl.gma.integracion.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

@JacksonXmlRootElement(localName = "pedido")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoWeb {

    public String fecha;
    public CompradorWeb comprador;
    public DireccionWeb direccionDespacho;

    @JacksonXmlElementWrapper(localName = "items")
    @JacksonXmlProperty(localName = "item")
    public List<ItemWeb> items;

    // --- CLASES INTERNAS ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompradorWeb {
        public String rut;
        public String nombre;
        public String apellido;
        public String email;
        public String telefono;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DireccionWeb {
        // En tu XML a veces viene "calleYNumero", a veces "calle". 
        // Ponemos ambos para que no falle.
        public String calle;
        public String calleYNumero; 
        public String numero;
        public String comuna;
        public String ciudad;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemWeb {
        public Integer cantidad;
        
        // CORRECCIÓN: El SKU, Nombre y Precio vienen dentro de "producto"
        public ProductoWeb producto;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductoWeb {
        public String sku;
        public String nombre;
        
        // En el XML de TiendaWeb se llama "precioLista" o "costo"
        @JacksonXmlProperty(localName = "precioLista") 
        public Integer precio; 
    }
}