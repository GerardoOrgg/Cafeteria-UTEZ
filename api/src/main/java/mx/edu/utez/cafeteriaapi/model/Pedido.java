package mx.edu.utez.cafeteriaapi.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

// Tabla "pedidos". Cada pedido pertenece a UNA cafetería: solo su encargado lo ve en Comandas
@Entity
@Table(name = "pedidos")
public class Pedido {

    // El número que se le dice al alumno ("Pedido #12"); lo genera MySQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer numero;

    private Integer usuarioId;

    @Column(nullable = false, length = 100)
    private String clienteNombre;

    @Column(nullable = false, length = 100)
    private String matricula;

    @Column(nullable = false, length = 20)
    private String cafeteriaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.RECIBIDO;

    // Milisegundos desde 1970 (así lo usa la app para mostrar "Hoy 11:54 AM")
    private long creadoEn;

    @Column(length = 200)
    private String nota = "";

    @Column(length = 30)
    private String mostrador = "Mostrador 1";

    // Minutos que tardó, se calcula al entregar
    private Integer minutosPrep;

    // Motivo de cancelación
    @Column(length = 200)
    private String motivo;

    // Los platillos del pedido, en la tabla "pedido_lineas"
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_lineas", joinColumns = @JoinColumn(name = "pedido_numero"))
    @OrderColumn(name = "orden")
    private List<LineaPedido> lineas = new ArrayList<>();

    public Pedido() {
    }

    public int total() {
        return lineas.stream().mapToInt(LineaPedido::subtotal).sum();
    }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCafeteriaId() { return cafeteriaId; }
    public void setCafeteriaId(String cafeteriaId) { this.cafeteriaId = cafeteriaId; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public long getCreadoEn() { return creadoEn; }
    public void setCreadoEn(long creadoEn) { this.creadoEn = creadoEn; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public String getMostrador() { return mostrador; }
    public void setMostrador(String mostrador) { this.mostrador = mostrador; }

    public Integer getMinutosPrep() { return minutosPrep; }
    public void setMinutosPrep(Integer minutosPrep) { this.minutosPrep = minutosPrep; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public List<LineaPedido> getLineas() { return lineas; }
    public void setLineas(List<LineaPedido> lineas) { this.lineas = lineas; }
}
