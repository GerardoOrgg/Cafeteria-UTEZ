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

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20)
    private String cafeteriaId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String descripcion = "";

    @Column(nullable = false)
    private Integer precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    private Integer dia;

    @Column(length = 1000)
    private String detalle = "";

    @Column(length = 30)
    private String etiqueta;

    @Column(length = 200)
    private String foto;

    private boolean disponible = true;

    private int prepMin = 5;

    private int prepMax = 10;

    @Column(length = 80)
    private String grupoOpciones;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_opciones", joinColumns = @JoinColumn(name = "producto_id"))
    @OrderColumn(name = "orden")
    private List<Opcion> opciones = new ArrayList<>();

    @Column(length = 200)
    private String recomendacion;

    private long creadoEn = System.currentTimeMillis();

    public Producto() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCafeteriaId() { return cafeteriaId; }
    public void setCafeteriaId(String cafeteriaId) { this.cafeteriaId = cafeteriaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Integer getDia() { return dia; }
    public void setDia(Integer dia) { this.dia = dia; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public int getPrepMin() { return prepMin; }
    public void setPrepMin(int prepMin) { this.prepMin = prepMin; }

    public int getPrepMax() { return prepMax; }
    public void setPrepMax(int prepMax) { this.prepMax = prepMax; }

    public String getGrupoOpciones() { return grupoOpciones; }
    public void setGrupoOpciones(String grupoOpciones) { this.grupoOpciones = grupoOpciones; }

    public List<Opcion> getOpciones() { return opciones; }
    public void setOpciones(List<Opcion> opciones) { this.opciones = opciones; }

    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }

    public long getCreadoEn() { return creadoEn; }
    public void setCreadoEn(long creadoEn) { this.creadoEn = creadoEn; }
}
