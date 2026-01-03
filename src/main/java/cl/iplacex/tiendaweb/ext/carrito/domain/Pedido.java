package cl.iplacex.tiendaweb.ext.carrito.domain;

import cl.iplacex.tiendaweb.ext.carrito.adapter.LocalDateTimeAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//Para la integracion 3.4
import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "pedido") //// Se especifica el nombre para mayor claridad en el XML
public class Pedido implements Serializable { // Implementa Serializable para la integración JMS

    //Agregado para integración JMS
    private static final long serialVersionUID = 1L;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime fecha = LocalDateTime.now();

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<LineaPedidoImpl> items = new ArrayList<>();
    private Comprador comprador = new Comprador();
    private Direccion direccionDespacho = new Direccion();

    //Constructor vacio que se requiere para la serialización JAXB
    public Pedido() {}
    
    public Long getTotal() {
        return this.items.stream().mapToLong(LineaPedidoImpl::getSubtotal).sum();
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<LineaPedidoImpl> getItems() {
        return items;
    }

    public void setItems(List<LineaPedidoImpl> items) {
        this.items = items;
    }

    public Comprador getComprador() {
        return comprador;
    }

    public void setComprador(Comprador comprador) {
        this.comprador = comprador;
    }

    public Direccion getDireccionDespacho() {
        return direccionDespacho;
    }

    public void setDireccionDespacho(Direccion direccionDespacho) {
        this.direccionDespacho = direccionDespacho;
    }
}
