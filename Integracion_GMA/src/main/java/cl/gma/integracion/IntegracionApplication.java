package cl.gma.integracion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Esto activa el ciclo de lectura del Adapter
public class IntegracionApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegracionApplication.class, args);
    }
}