package mx.edu.utez.cafeteriaapi.controller;

import mx.edu.utez.cafeteriaapi.dto.Respuestas.MensajeDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InicioController {
    @GetMapping({"/", "/api"})
    public MensajeDto inicio() {
        return new MensajeDto("API Cafetería UTEZ funcionando");
    }
}
