package cl.gma.integracion.service;

import org.springframework.stereotype.Service;
import jakarta.xml.soap.*;
import java.net.URL;

@Service
public class ClienteFacturacionSoap {

    private final String endpoint = "http://localhost:8090/soap/facturacion";

    public void llamarFacturacion(String cliente, String rut, long total) {
        try {
            // 1. Crear la conexión y el mensaje
            MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();

            // 2. Definir el Body del XML según el WSDL
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration("tns", "http://example.org/");

            SOAPBody soapBody = envelope.getBody();
            SOAPElement operation = soapBody.addChildElement("generarBoleta", "tns");

            operation.addChildElement("cliente").addTextNode(cliente);
            operation.addChildElement("rut").addTextNode(rut);
            operation.addChildElement("total").addTextNode(String.valueOf(total));

            soapMessage.saveChanges();

            // 3. Enviar el mensaje
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            
            System.out.println(">>> [SOAP] Llamando a facturación para: " + cliente);
            SOAPMessage soapResponse = soapConnection.call(soapMessage, new URL(endpoint));

            // 4. Leer respuesta simple
            String respuesta = soapResponse.getSOAPBody().getElementsByTagName("boletaXml").item(0).getTextContent();
            System.out.println(">>> [SOAP] Factura generada exitosamente.");
            
            soapConnection.close();
        } catch (Exception e) {
            System.err.println(">>> [SOAP ERROR] No se pudo facturar: " + e.getMessage());
        }
    }
}