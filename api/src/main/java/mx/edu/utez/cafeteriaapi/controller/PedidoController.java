package mx.edu.utez.cafeteriaapi.controller;

import jakarta.validation.Valid;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.CancelarRequest;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.PedidoRequest;
import mx.edu.utez.cafeteriaapi.dto.Respuestas.PedidoDto;
import mx.edu.utez.cafeteriaapi.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PedidoDto> listar(@RequestParam(required = false) String cafeteriaId,
                                  @RequestParam(required = false) String matricula) {
        return servicio.listar(cafeteriaId, matricula).stream().map(PedidoDto::de).toList();
    }

    @GetMapping("/{numero}")
    public PedidoDto obtener(@PathVariable Integer numero) {
        return PedidoDto.de(servicio.obtener(numero));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDto crear(@Valid @RequestBody PedidoRequest datos) {
        return PedidoDto.de(servicio.crear(datos));
    }

    @PatchMapping("/{numero}/avanzar")
    public PedidoDto avanzar(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @PathVariable Integer numero) {
        return PedidoDto.de(servicio.avanzar(numero, usuarioId));
    }

    @PatchMapping("/{numero}/cancelar")
    public PedidoDto cancelar(@RequestHeader(Encabezados.USUARIO) Integer usuarioId, @PathVariable Integer numero,
                              @RequestBody(required = false) CancelarRequest datos) {
        return PedidoDto.de(servicio.cancelar(numero, datos == null ? null : datos.motivo(), usuarioId));
    }
}
