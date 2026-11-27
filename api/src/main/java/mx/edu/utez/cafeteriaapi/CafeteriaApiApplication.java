package mx.edu.utez.cafeteriaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API REST de la app Cafetería UTEZ.
 * La app Android le pide aquí los menús y los pedidos, así los dos teléfonos
 * (alumno y encargado) ven la misma información.
 */
@SpringBootApplication
public class CafeteriaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeteriaApiApplication.class, args);
    }
}
