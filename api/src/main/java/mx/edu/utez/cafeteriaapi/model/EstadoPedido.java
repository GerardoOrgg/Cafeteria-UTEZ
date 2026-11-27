package mx.edu.utez.cafeteriaapi.model;

public enum EstadoPedido {
    RECIBIDO,
    PREPARANDO,
    LISTO,
    ENTREGADO,
    CANCELADO;

    public boolean esActivo() {
        return this == RECIBIDO || this == PREPARANDO || this == LISTO;
    }
}
