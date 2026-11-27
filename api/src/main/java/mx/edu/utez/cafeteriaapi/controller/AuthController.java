package mx.edu.utez.cafeteriaapi.controller;

import jakarta.validation.Valid;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.LoginRequest;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.RegistroRequest;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.RestablecerRequest;
import mx.edu.utez.cafeteriaapi.dto.Respuestas.MensajeDto;
import mx.edu.utez.cafeteriaapi.dto.Respuestas.UsuarioDto;
import mx.edu.utez.cafeteriaapi.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public UsuarioDto login(@Valid @RequestBody LoginRequest datos) {
        return UsuarioDto.de(auth.iniciarSesion(datos));
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registro(@Valid @RequestBody RegistroRequest datos) {
        return UsuarioDto.de(auth.registrarAlumno(datos));
    }

    @PostMapping("/restablecer")
    public MensajeDto restablecer(@Valid @RequestBody RestablecerRequest datos) {
        auth.restablecer(datos);
        return new MensajeDto("Contraseña actualizada. Ya puedes iniciar sesión.");
    }
}
