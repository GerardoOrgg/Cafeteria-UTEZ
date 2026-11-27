package mx.edu.utez.cafeteriaapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

// Copia del platillo dentro de un pedido (si luego se edita o borra el platillo, el pedido no cambia)
@Embeddable
public class LineaPedido {

    private Integer productoId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 80)
    private String opcion;

    @Column(length = 120)
    private String nota;

    // Precio por pieza, ya con el extra de la opción
    private int precio;

    private int cantidad;

    @Column(length = 200)
    private String foto;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Categoria categoria;

    public LineaPedido() {
    }

    public LineaPedido(Producto p, String opcion, String nota, int precio, int cantidad) {
        this.productoId = p.getId();
        this.nombre = p.getNombre();
        this.opcion = opcion;
        this.nota = nota;
        this.precio = precio;
        this.cantidad = cantidad;
        this.foto = p.getFoto();
        this.categoria = p.getCategoria();
    }

    public int subtotal() {
        return precio * cantidad;
    }

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getOpcion() { return opcion; }
    public void setOpcion(String opcion) { this.opcion = opcion; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
