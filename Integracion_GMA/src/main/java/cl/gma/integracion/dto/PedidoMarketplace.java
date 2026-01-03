package cl.gma.integracion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoMarketplace {
    
    private String id;
    private String fecha;
    private Map<String, Object> cliente;
    private Map<String, Object> direccion;
    private List<Map<String, Object>> items;
    
    // ESTE ES EL DATO QUE VAMOS A AGREGAR (Enriquecimiento)
    private Integer costoEnvio; 

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Map<String, Object> getCliente() { return cliente; }
    public void setCliente(Map<String, Object> cliente) { this.cliente = cliente; }

    public Map<String, Object> getDireccion() { return direccion; }
    public void setDireccion(Map<String, Object> direccion) { this.direccion = direccion; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }

    public Integer getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(Integer costoEnvio) { this.costoEnvio = costoEnvio; }
}