package cl.gma.integracion.modelo;

public class MensajeCanonico {
    private Metadata canonicalMetadata;
    private PedidoCanonico order;

    public Metadata getCanonicalMetadata() { return canonicalMetadata; }
    public void setCanonicalMetadata(Metadata canonicalMetadata) { this.canonicalMetadata = canonicalMetadata; }

    public PedidoCanonico getOrder() { return order; }
    public void setOrder(PedidoCanonico order) { this.order = order; }

    // Clase interna pequeña solo para metadata (o puedes sacarla a otro archivo)
    public static class Metadata {
        public String schemaVersion = "1.0";
        public String traceId;
        public String processedAt;
    }
}