package mx.edu.utez.cafeteriaapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Opcion {
    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 200)
    private String detalle = "";

    private int extra;

    public Opcion() {
    }

    public Opcion(String nombre, int extra, String detalle) {
        this.nombre = nombre;
        this.extra = extra;
        this.detalle = detalle;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public int getExtra() { return extra; }
    public void setExtra(int extra) { this.extra = extra; }
}
