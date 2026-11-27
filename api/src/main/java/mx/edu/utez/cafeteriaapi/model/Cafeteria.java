package mx.edu.utez.cafeteriaapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla "cafeterias": las dos cafeterías y su tiempo de espera actual
@Entity
@Table(name = "cafeterias")
public class Cafeteria {

    // "nido" o "balcon" (el mismo id que usa la app)
    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TiempoEspera tiempoEspera = TiempoEspera.MEDIO;

    public Cafeteria() {
    }

    public Cafeteria(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TiempoEspera getTiempoEspera() { return tiempoEspera; }
    public void setTiempoEspera(TiempoEspera tiempoEspera) { this.tiempoEspera = tiempoEspera; }
}
