package mx.edu.utez.cafeteriaapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cafeterias")
public class Cafeteria {
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
