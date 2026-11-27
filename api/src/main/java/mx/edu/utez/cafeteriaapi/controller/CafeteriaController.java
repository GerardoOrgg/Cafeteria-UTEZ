package mx.edu.utez.cafeteriaapi.controller;

import jakarta.validation.Valid;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.TiempoRequest;
import mx.edu.utez.cafeteriaapi.dto.Respuestas.CafeteriaDto;
import mx.edu.utez.cafeteriaapi.exception.ApiException;
import mx.edu.utez.cafeteriaapi.model.Cafeteria;
import mx.edu.utez.cafeteriaapi.model.TiempoEspera;
import mx.edu.utez.cafeteriaapi.repository.CafeteriaRepository;
import mx.edu.utez.cafeteriaapi.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/cafeterias              -> las dos cafeterías con su tiempo de espera
 * PUT /api/cafeterias/{id}/tiempo  -> el encargado cambia el tiempo de espera
 */
@RestController
@RequestMapping("/api/cafeterias")
public class CafeteriaController {

    private final CafeteriaRepository cafeterias;
    private final AuthService auth;

    public CafeteriaController(CafeteriaRepository cafeterias, AuthService auth) {
        this.cafeterias = cafeterias;
        this.auth = auth;
    }

    @GetMapping
    public List<CafeteriaDto> listar() {
        return cafeterias.findAll().stream().map(CafeteriaDto::de).toList();
    }

    @PutMapping("/{id}/tiempo")
    public CafeteriaDto cambiarTiempo(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @PathVariable String id,
                                      @Valid @RequestBody TiempoRequest datos) {
        auth.exigirEncargado(usuarioId, id);
        Cafeteria c = cafeterias.findById(id).orElseThrow(() -> ApiException.noEncontrado("Esa cafetería no existe."));
        try {
            c.setTiempoEspera(TiempoEspera.valueOf(datos.tiempo()));
        } catch (IllegalArgumentException e) {
            throw ApiException.invalido("Tiempo de espera desconocido: " + datos.tiempo());
        }
        return CafeteriaDto.de(cafeterias.save(c));
    }
}
