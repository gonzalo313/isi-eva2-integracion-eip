package cl.gma.integracion.modelo;

import java.io.Serializable;

public class ItemCanonico implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sku;
    private String name;       
    private Integer quantity;  
    private Integer unitPrice; 
    private Integer totalPrice; 
    private Double taxRate;    

    public ItemCanonico() {}

    // Constructor actualizado
    public ItemCanonico(String sku, String name, Integer quantity, Integer unitPrice, Integer totalPrice) {
        this.sku = sku;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // --- GETTERS Y SETTERS (En Inglés para coincidir con el Traductor) ---

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Integer unitPrice) { this.unitPrice = unitPrice; }

    public Integer getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    
    public Double getTaxRate() { return taxRate; }
    public void setTaxRate(Double taxRate) { this.taxRate = taxRate; }
}