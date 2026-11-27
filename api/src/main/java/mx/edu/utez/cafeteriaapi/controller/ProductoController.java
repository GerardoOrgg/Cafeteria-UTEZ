package mx.edu.utez.cafeteriaapi.controller;

import jakarta.validation.Valid;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.DisponibleRequest;
import mx.edu.utez.cafeteriaapi.dto.Respuestas.MensajeDto;
import mx.edu.utez.cafeteriaapi.dto.Respuestas.ProductoDto;
import mx.edu.utez.cafeteriaapi.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ProductoDto> listar(@RequestParam(required = false) String cafeteriaId) {
        return servicio.listar(cafeteriaId).stream().map(ProductoDto::de).toList();
    }

    @GetMapping("/{id}")
    public ProductoDto obtener(@PathVariable Integer id) {
        return ProductoDto.de(servicio.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDto crear(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @RequestBody ProductoDto datos) {
        return ProductoDto.de(servicio.crear(datos, usuarioId));
    }

    @PutMapping("/{id}")
    public ProductoDto actualizar(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @PathVariable Integer id,
                                  @RequestBody ProductoDto datos) {
        return ProductoDto.de(servicio.actualizar(id, datos, usuarioId));
    }

    @PatchMapping("/{id}/disponible")
    public ProductoDto cambiarDisponible(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @PathVariable Integer id,
                                         @Valid @RequestBody DisponibleRequest datos) {
        return ProductoDto.de(servicio.cambiarDisponible(id, datos.disponible(), usuarioId));
    }

    @DeleteMapping("/{id}")
    public MensajeDto borrar(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @PathVariable Integer id) {
        servicio.borrar(id, usuarioId);
        return new MensajeDto("Platillo borrado.");
    }
}
