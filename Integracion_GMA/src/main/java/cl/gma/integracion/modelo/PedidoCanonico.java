package cl.gma.integracion.modelo;

import java.util.List;

public class PedidoCanonico {
    private Encabezado header;
    private Cliente customer;
    private Direccion shippingAddress;
    private Direccion billingAddress;
    private List<ItemCanonico> items;
    private Finanzas financials;

    // Getters y Setters
    public Encabezado getHeader() { return header; }
    public void setHeader(Encabezado header) { this.header = header; }

    public Cliente getCustomer() { return customer; }
    public void setCustomer(Cliente customer) { this.customer = customer; }

    public Direccion getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Direccion shippingAddress) { this.shippingAddress = shippingAddress; }

    public Direccion getBillingAddress() { return billingAddress; }
    public void setBillingAddress(Direccion billingAddress) { this.billingAddress = billingAddress; }

    public List<ItemCanonico> getItems() { return items; }
    public void setItems(List<ItemCanonico> items) { this.items = items; }

    public Finanzas getFinancials() { return financials; }
    public void setFinancials(Finanzas financials) { this.financials = financials; }
}