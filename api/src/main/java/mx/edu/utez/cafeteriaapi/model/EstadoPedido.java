package mx.edu.utez.cafeteriaapi.model;

// RECIBIDO -> PREPARANDO -> LISTO -> ENTREGADO (o CANCELADO)
public enum EstadoPedido {
    RECIBIDO,
    PREPARANDO,
    LISTO,
    ENTREGADO,
    CANCELADO;

    // Un pedido activo todavía aparece en Comandas
    public boolean esActivo() {
        return this == RECIBIDO || this == PREPARANDO || this == LISTO;
    }
}
