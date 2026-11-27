package mx.edu.utez.cafeteriaapi.config;

import mx.edu.utez.cafeteriaapi.model.Cafeteria;
import mx.edu.utez.cafeteriaapi.model.Categoria;
import mx.edu.utez.cafeteriaapi.model.EstadoPedido;
import mx.edu.utez.cafeteriaapi.model.LineaPedido;
import mx.edu.utez.cafeteriaapi.model.Opcion;
import mx.edu.utez.cafeteriaapi.model.Pedido;
import mx.edu.utez.cafeteriaapi.model.Producto;
import mx.edu.utez.cafeteriaapi.model.Rol;
import mx.edu.utez.cafeteriaapi.model.Usuario;
import mx.edu.utez.cafeteriaapi.repository.CafeteriaRepository;
import mx.edu.utez.cafeteriaapi.repository.PedidoRepository;
import mx.edu.utez.cafeteriaapi.repository.ProductoRepository;
import mx.edu.utez.cafeteriaapi.repository.UsuarioRepository;
import mx.edu.utez.cafeteriaapi.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static mx.edu.utez.cafeteriaapi.model.Categoria.BEBIDAS;
import static mx.edu.utez.cafeteriaapi.model.Categoria.DIARIO;
import static mx.edu.utez.cafeteriaapi.model.Categoria.ESPECIAL;
import static mx.edu.utez.cafeteriaapi.model.Categoria.POSTRES;
import static mx.edu.utez.cafeteriaapi.model.Categoria.SNACKS;

@Component
public class DatosIniciales implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private static final String NIDO = "nido";
    private static final String BALCON = "balcon";

    private static final String ALUMNO_NOMBRE = "Juan Pérez";
    private static final String ALUMNO_MATRICULA = "20213TI045";

    private final UsuarioRepository usuarios;
    private final CafeteriaRepository cafeterias;
    private final ProductoRepository productos;
    private final PedidoRepository pedidos;
    private final ZoneId zona;

    public DatosIniciales(UsuarioRepository usuarios, CafeteriaRepository cafeterias, ProductoRepository productos,
                          PedidoRepository pedidos, @Value("${app.zona-horaria:America/Mexico_City}") String zona) {
        this.usuarios = usuarios;
        this.cafeterias = cafeterias;
        this.productos = productos;
        this.pedidos = pedidos;
        this.zona = ZoneId.of(zona);
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (cafeterias.count() == 0) {
            cafeterias.save(new Cafeteria(NIDO, "Cafetería Nido de Halcón"));
            cafeterias.save(new Cafeteria(BALCON, "Cafetería El Balcón"));
        }
        if (usuarios.count() == 0) {
            usuarios.save(new Usuario(ALUMNO_NOMBRE, ALUMNO_MATRICULA, ALUMNO_MATRICULA.toLowerCase() + "@utez.edu.mx",
                    AuthService.hash("123456"), Rol.ALUMNO, null));
            usuarios.save(new Usuario("Encargado Nido de Halcón", "nido@utez.edu.mx", "nido@utez.edu.mx",
                    AuthService.hash("nido123"), Rol.ENCARGADO, NIDO));
            usuarios.save(new Usuario("Encargado El Balcón", "balcon@utez.edu.mx", "balcon@utez.edu.mx",
                    AuthService.hash("balcon123"), Rol.ENCARGADO, BALCON));
        }
        if (productos.count() == 0) {
            List<Producto> menu = new ArrayList<>();
            for (Plato plato : nido()) menu.add(plato.p);
            for (Plato plato : balcon()) menu.add(plato.p);
            List<Producto> guardados = productos.saveAll(menu);
            pedidosDeEjemplo(guardados);
            log.info("Base de datos llenada con {} platillos y pedidos de ejemplo", guardados.size());
        }
    }

    private static Opcion o(String nombre) {
        return new Opcion(nombre, 0, "");
    }

    private static Opcion o(String nombre, int extra) {
        return new Opcion(nombre, extra, "");
    }

    private static Opcion od(String nombre, String detalle) {
        return new Opcion(nombre, 0, detalle);
    }

    private static Opcion[] tortilla() {
        return new Opcion[]{o("Maíz"), o("Harina", 5)};
    }

    private static Opcion[] acompanamiento() {
        return new Opcion[]{o("Papas a la francesa"), o("Aros de cebolla", 10)};
    }

    private static Opcion[] sabores() {
        return new Opcion[]{o("Horchata"), o("Jamaica"), o("Limón")};
    }

    private static Opcion[] pozoles() {
        return new Opcion[]{o("Rojo"), o("Blanco"), o("Verde")};
    }

    private static Plato p(String cafeteria, Categoria categoria, String nombre, String descripcion, int precio) {
        return new Plato(cafeteria, categoria, nombre, descripcion, precio);
    }

    private static List<Plato> nido() {
        return List.of(
                p(NIDO, ESPECIAL, "Taco Acorazado de Milanesa", "Con arroz rojo y frijol de la olla", 55).dia(1)
                        .opciones("Tipo de milanesa",
                                od("Pollo empanizado", "Pechuga suave, dorada en freidora"),
                                od("Cerdo crujiente", "Corte tradicional, textura extra crocante"))
                        .detalle("Doble tortilla de comal con generosa cama de arroz rojo tradicional, milanesa de cerdo o pollo crujiente empanizada al momento, acompañada de rajas o salsa al gusto.")
                        .etiqueta("Favorito").prep(8, 12).recomendacion("Porción individual recomendada: 1–2 pzas"),
                p(NIDO, ESPECIAL, "Taco Acorazado de Huevo", "Doble ración con arroz y frijoles", 50).dia(1)
                        .opciones("Estilo del huevo", o("Duro tradicional"), o("Revuelto a la mexicana"))
                        .detalle("Doble tortilla con arroz rojo, frijoles y huevo preparado al momento.").prep(6, 10)
                        .recomendacion("Porción individual recomendada: 1–2 pzas"),
                p(NIDO, ESPECIAL, "Taco Acorazado de Chile Relleno", "Chile poblano relleno, capeado", 55).dia(1)
                        .opciones("Relleno", o("Queso panela"), o("Picadillo", 5))
                        .detalle("Chile poblano capeado sobre doble tortilla y cama de arroz rojo.").prep(10, 14).agotado(),
                p(NIDO, ESPECIAL, "Taco Acorazado de Papa con Longaniza", "Guisado casero de Yecapixtla", 50).dia(1)
                        .detalle("Papa con longaniza de Yecapixtla sobre doble tortilla y arroz rojo.").prep(6, 10),
                p(NIDO, ESPECIAL, "Medio Taco Acorazado", "Una tortilla, media porción", 30).dia(1)
                        .opciones("Guisado", o("Milanesa de pollo"), o("Milanesa de cerdo"), o("Huevo"), o("Papa con longaniza"))
                        .detalle("Para cuando no tienes tanta hambre: una tortilla con arroz y el guisado que elijas.").prep(5, 8),

                p(NIDO, ESPECIAL, "Orden de tacos al pastor (5)", "Con piña, cebolla y cilantro", 60).dia(2)
                        .opciones("Tortilla", tortilla()).etiqueta("Favorito").prep(6, 10)
                        .detalle("Cinco tacos de pastor del trompo con piña, cebolla, cilantro y salsas de la casa."),
                p(NIDO, ESPECIAL, "Orden de tacos de bistec (5)", "Con cebolla asada y cilantro", 65).dia(2)
                        .opciones("Tortilla", tortilla()).prep(6, 10)
                        .detalle("Cinco tacos de bistec a la plancha con cebolla asada, cilantro y limón."),
                p(NIDO, ESPECIAL, "Orden de tacos de barbacoa (4)", "De res, con consomé chico", 70).dia(2)
                        .opciones("Tortilla", tortilla()).prep(5, 8)
                        .detalle("Cuatro tacos de barbacoa de res con cebolla, cilantro y un consomé chico."),
                p(NIDO, ESPECIAL, "Taco suelto", "Uno solo, el que quieras", 15).dia(2)
                        .opciones("Carne", o("Pastor"), o("Bistec"), o("Barbacoa", 3)).prep(3, 6)
                        .recomendacion("Pide los que quieras: se cobran por pieza"),
                p(NIDO, ESPECIAL, "Gringa de pastor", "Tortilla de harina con queso y pastor", 45).dia(2).prep(6, 10),
                p(NIDO, ESPECIAL, "Consomé de barbacoa", "Vaso con garbanzo y arroz", 25).dia(2).prep(2, 4),

                p(NIDO, ESPECIAL, "Enchiladas rojas o verdes (4)", "Con crema, queso y cebolla", 55).dia(3)
                        .opciones("Salsa", o("Verde"), o("Roja")).prep(8, 12)
                        .detalle("Cuatro enchiladas rellenas de pollo bañadas en salsa, con crema, queso y cebolla."),
                p(NIDO, ESPECIAL, "Enmoladas (4)", "Bañadas en mole poblano", 60).dia(3)
                        .opciones("Relleno", o("Pollo"), o("Queso")).prep(8, 12)
                        .detalle("Cuatro tortillas rellenas bañadas en mole, con ajonjolí y cebolla."),
                p(NIDO, ESPECIAL, "Enchiladas suizas (4)", "Gratinadas con crema", 60).dia(3)
                        .opciones("Relleno", o("Pollo"), o("Queso")).etiqueta("Favorito").prep(10, 14)
                        .detalle("Salsa verde con crema, gratinadas con queso al horno."),
                p(NIDO, ESPECIAL, "Media orden de enchiladas (2)", "Para probar", 35).dia(3)
                        .opciones("Tipo", o("Verdes"), o("Rojas"), o("Enmoladas", 5), o("Suizas", 5)).prep(6, 10),

                p(NIDO, ESPECIAL, "Pozole chico", "Con lechuga, rábano, orégano y limón", 55).dia(4)
                        .opciones("Tipo de pozole", pozoles()).prep(3, 6)
                        .detalle("Pozole de cerdo con maíz cacahuazintle. Se acompaña con tostadas."),
                p(NIDO, ESPECIAL, "Pozole grande", "Con lechuga, rábano, orégano y limón", 75).dia(4)
                        .opciones("Tipo de pozole", pozoles()).etiqueta("Favorito").prep(3, 6)
                        .detalle("Plato grande de pozole de cerdo con maíz cacahuazintle. Se acompaña con tostadas."),
                p(NIDO, ESPECIAL, "Tostada", "Con crema, lechuga y queso", 18).dia(4)
                        .opciones("De", o("Tinga"), o("Pata"), o("Pollo")).prep(3, 5),
                p(NIDO, ESPECIAL, "Orden de flautas (4)", "Con crema, queso y salsa", 50).dia(4)
                        .opciones("Relleno", o("Pollo"), o("Papa"), o("Res", 5)).prep(8, 12),

                p(NIDO, ESPECIAL, "Hamburguesa sencilla", "Res, queso, jitomate y lechuga, con papas", 60).dia(5)
                        .opciones("Acompañamiento", acompanamiento()).etiqueta("Favorito").prep(10, 15)
                        .detalle("Carne de res al carbón, queso amarillo, jitomate, lechuga y cebolla en pan suave."),
                p(NIDO, ESPECIAL, "Hamburguesa doble", "Doble carne y doble queso, con papas", 80).dia(5)
                        .opciones("Acompañamiento", acompanamiento()).prep(12, 16),
                p(NIDO, ESPECIAL, "Hamburguesa hawaiana", "Con piña y jamón, con papas", 75).dia(5)
                        .opciones("Acompañamiento", acompanamiento()).prep(12, 16),
                p(NIDO, ESPECIAL, "Hot dog", "Salchicha de pavo con cebolla y jitomate", 30).dia(5)
                        .opciones("Estilo", o("Sencillo"), o("Con tocino", 10)).prep(4, 7),
                p(NIDO, ESPECIAL, "Papas a la francesa", "Con catsup y salsa", 30).dia(5)
                        .opciones("Tamaño", o("Mediana"), o("Grande", 10)).prep(5, 8),

                p(NIDO, DIARIO, "Chilaquiles", "Con pollo, crema y queso", 55)
                        .opciones("Salsa", o("Verde"), o("Roja")).etiqueta("Favorito")
                        .foto("res:food_chilaquiles").prep(6, 10)
                        .detalle("Totopos bañados en salsa con pollo deshebrado, crema, queso fresco y cebolla."),
                p(NIDO, DIARIO, "Torta", "Con aguacate, frijol y queso", 45)
                        .opciones("Tipo de torta", o("Milanesa"), o("Pechuga"), o("Jamón"), o("Pierna", 5))
                        .etiqueta("Rápido").foto("res:food_torta").prep(5, 8),
                p(NIDO, DIARIO, "Molletes (3)", "Frijoles, queso gratinado y pico de gallo", 40)
                        .opciones("Agregar", o("Solo queso"), o("Con chorizo", 10), o("Con jamón", 8)).prep(6, 10),
                p(NIDO, DIARIO, "Tacos dorados (4)", "Con lechuga, crema y salsa", 45)
                        .opciones("Relleno", o("Pollo"), o("Papa"), o("Res", 5)).prep(8, 12),
                p(NIDO, DIARIO, "Quesadillas (3)", "Tortilla de maíz hecha a mano", 45)
                        .opciones("Relleno", o("Queso"), o("Tinga", 10), o("Champiñones", 5), o("Chicharrón", 10)).prep(6, 10),
                p(NIDO, DIARIO, "Sopes (2)", "Con frijol, crema, queso y lechuga", 40)
                        .opciones("Guisado", o("Tinga"), o("Chorizo"), o("Solo frijol")).prep(6, 10),
                p(NIDO, DIARIO, "Enchiladas (4)", "Rellenas de pollo, con crema y queso", 55)
                        .opciones("Salsa", o("Verdes"), o("Rojas")).foto("res:food_enchiladas").prep(8, 12),
                p(NIDO, DIARIO, "Huevos al gusto", "Con frijoles y tortillas", 48)
                        .opciones("Estilo", o("A la mexicana"), o("Con jamón"), o("Con chorizo"), o("Rancheros"))
                        .foto("res:food_huevos").prep(6, 10),
                p(NIDO, DIARIO, "Pechuga asada con ensalada", "Ligera, sazonada al comal", 60)
                        .etiqueta("Fit").foto("res:food_pechuga").prep(10, 14),
                p(NIDO, DIARIO, "Comida corrida", "Sopa del día, guisado y agua", 65)
                        .opciones("Sopa", o("Pasta"), o("Consomé")).foto("res:food_sopa").prep(3, 6)
                        .detalle("Sopa del día, guisado del día con arroz y frijoles, tortillas y agua fresca chica."),

                p(NIDO, BEBIDAS, "Agua fresca 500 ml", "Del día", 20).opciones("Sabor", sabores()).prep(1, 2),
                p(NIDO, BEBIDAS, "Agua fresca 1 L", "Del día", 35).opciones("Sabor", sabores()).prep(1, 2),
                p(NIDO, BEBIDAS, "Refresco 600 ml", "Bien frío", 22)
                        .opciones("Sabor", o("Cola"), o("Naranja"), o("Manzana"), o("Limón")).prep(1, 1),
                p(NIDO, BEBIDAS, "Café de olla", "Con canela y piloncillo", 15).prep(2, 3),
                p(NIDO, BEBIDAS, "Agua natural 600 ml", "Embotellada", 12).prep(1, 1),

                p(NIDO, SNACKS, "Fruta picada", "Vaso con fruta de temporada", 25)
                        .opciones("Con", o("Chile y limón"), o("Sin nada")).prep(2, 4),
                p(NIDO, SNACKS, "Papas de bolsa", "Varias marcas", 18).prep(1, 1),

                p(NIDO, POSTRES, "Gelatina", "Vaso individual", 15).opciones("Sabor", o("Leche"), o("Fresa"), o("Mosaico")).prep(1, 1),
                p(NIDO, POSTRES, "Arroz con leche", "Con canela", 18).prep(1, 1)
        );
    }

    private static List<Plato> balcon() {
        return List.of(
                p(BALCON, DIARIO, "Sándwich", "Pan de caja, lechuga y jitomate", 40)
                        .opciones("Relleno", o("Jamón y queso"), o("Pavo", 5), o("Panela")).etiqueta("Rápido").prep(4, 6),
                p(BALCON, DIARIO, "Baguette", "Pan recién horneado", 55)
                        .opciones("Relleno", o("Pavo"), o("Pollo"), o("Serrano", 15)).prep(6, 8),
                p(BALCON, DIARIO, "Wrap de pollo", "Tortilla de harina con pollo y verduras", 50).prep(5, 8),
                p(BALCON, DIARIO, "Ensalada César", "Lechuga, crutones y aderezo", 60)
                        .opciones("Proteína", o("Con pollo"), o("Sin proteína")).etiqueta("Fit").prep(5, 8),
                p(BALCON, DIARIO, "Cuernito relleno", "Cuernito de mantequilla", 35)
                        .opciones("Relleno", o("Jamón y queso"), o("Pavo y panela", 5)).prep(4, 6),

                p(BALCON, BEBIDAS, "Café americano", "Recién hecho", 20)
                        .opciones("Tamaño", o("12 oz"), o("16 oz", 8)).prep(2, 3),
                p(BALCON, BEBIDAS, "Capuchino", "Con espuma de leche", 35)
                        .opciones("Leche", o("Entera"), o("Deslactosada"), o("Almendra", 8)).prep(3, 5),
                p(BALCON, BEBIDAS, "Latte", "Espresso con leche", 38)
                        .opciones("Sabor", o("Natural"), o("Vainilla", 5), o("Caramelo", 5)).prep(3, 5),
                p(BALCON, BEBIDAS, "Frappé", "Con crema batida", 50)
                        .opciones("Sabor", o("Moka"), o("Vainilla"), o("Cajeta"), o("Galleta", 5)).etiqueta("Favorito").prep(4, 6),
                p(BALCON, BEBIDAS, "Té chai", "Especiado", 35).opciones("Estilo", o("Caliente"), o("Frío")).prep(3, 5),
                p(BALCON, BEBIDAS, "Smoothie", "De fruta natural", 45)
                        .opciones("Sabor", o("Fresa con plátano"), o("Mango"), o("Frutos rojos")).prep(4, 6),
                p(BALCON, BEBIDAS, "Jugo natural", "Recién exprimido", 30).opciones("Sabor", o("Naranja"), o("Verde")).prep(3, 5),
                p(BALCON, BEBIDAS, "Agua embotellada", "600 ml", 12).prep(1, 1),

                p(BALCON, SNACKS, "Yogurt con granola", "Con fruta y miel", 35).etiqueta("Fit").prep(2, 3),
                p(BALCON, SNACKS, "Fruta picada", "Vaso con fruta de temporada", 25).prep(2, 3),
                p(BALCON, SNACKS, "Barra de granola", "Avena y miel", 15).prep(1, 1),
                p(BALCON, SNACKS, "Palomitas", "Bolsa mediana", 20).opciones("Sabor", o("Mantequilla"), o("Caramelo")).prep(1, 2),
                p(BALCON, SNACKS, "Papas de bolsa", "Varias marcas", 18).prep(1, 1),

                p(BALCON, POSTRES, "Concha", "Pan dulce del día", 12).opciones("Sabor", o("Vainilla"), o("Chocolate")).prep(1, 1),
                p(BALCON, POSTRES, "Dona glaseada", "Esponjosa", 15).prep(1, 1),
                p(BALCON, POSTRES, "Muffin", "Horneado hoy", 25).opciones("Sabor", o("Chocolate"), o("Moras"), o("Plátano")).prep(1, 1),
                p(BALCON, POSTRES, "Brownie", "Con nuez", 25).prep(1, 1),
                p(BALCON, POSTRES, "Rebanada de pay de queso", "Con mermelada", 35).prep(1, 2),
                p(BALCON, POSTRES, "Galleta de avena", "Grande", 12).prep(1, 1)
        );
    }

    private void pedidosDeEjemplo(List<Producto> menu) {
        Integer juanId = usuarios.findFirstByMatriculaIgnoreCaseOrCorreoIgnoreCase(ALUMNO_MATRICULA, ALUMNO_MATRICULA)
                .map(Usuario::getId).orElse(null);

        Pedido p1 = pedido(juanId, ALUMNO_NOMBRE, ALUMNO_MATRICULA, NIDO, EstadoPedido.ENTREGADO, momento(8, 10, 5));
        p1.setMinutosPrep(6);
        linea(p1, menu, "Torta", 1, "Milanesa", null);
        linea(p1, menu, "Café de olla", 1, null, null);

        Pedido p2 = pedido(juanId, ALUMNO_NOMBRE, ALUMNO_MATRICULA, BALCON, EstadoPedido.ENTREGADO, momento(9, 30, 4));
        p2.setMinutosPrep(4);
        linea(p2, menu, "Capuchino", 1, null, null);
        linea(p2, menu, "Concha", 1, "Chocolate", null);

        Pedido p3 = pedido(null, "Luis Gómez", "20212DN044", NIDO, EstadoPedido.CANCELADO, momento(11, 5, 0));
        p3.setMotivo("Falta de ingrediente (Chile poblano)");
        linea(p3, menu, "Taco Acorazado de Chile Relleno", 1, null, null);

        Pedido p4 = pedido(null, "Sofía Morales", "20233TI101", NIDO, EstadoPedido.ENTREGADO, momento(11, 21, 0));
        p4.setMinutosPrep(8);
        linea(p4, menu, "Chilaquiles", 1, "Roja", null);

        Pedido p5 = pedido(null, "Carlos Ruiz", "20203ID088", NIDO, EstadoPedido.ENTREGADO, momento(11, 35, 0));
        p5.setMostrador("Mostrador 2");
        p5.setMinutosPrep(11);
        linea(p5, menu, "Torta", 2, "Pechuga", null);
        linea(p5, menu, "Refresco 600 ml", 1, null, null);

        Pedido p6 = pedido(null, "María Torres", "20223TE012", NIDO, EstadoPedido.ENTREGADO, momento(11, 42, 0));
        p6.setMinutosPrep(7);
        linea(p6, menu, "Quesadillas (3)", 1, "Tinga", null);
        linea(p6, menu, "Agua fresca 500 ml", 1, "Horchata", null);

        Pedido p7 = pedido(null, "Ana López", "20241TI007", NIDO, EstadoPedido.PREPARANDO, momento(11, 50, 0));
        linea(p7, menu, "Enchiladas (4)", 1, "Verdes", null);
        linea(p7, menu, "Agua fresca 500 ml", 1, "Jamaica", null);

        Pedido p8 = pedido(null, "Diego Hernández", "20232TI019", NIDO, EstadoPedido.RECIBIDO, momento(11, 54, 0));
        p8.setNota("Sin cebolla");
        linea(p8, menu, "Sopes (2)", 1, "Tinga", "Salsa aparte");
        linea(p8, menu, "Agua fresca 500 ml", 1, null, null);

        Pedido p9 = pedido(null, "Jorge Ramírez", "20221TI033", BALCON, EstadoPedido.ENTREGADO, momento(10, 40, 0));
        p9.setMinutosPrep(5);
        linea(p9, menu, "Café americano", 1, "16 oz", null);
        linea(p9, menu, "Muffin", 1, "Moras", null);

        Pedido p10 = pedido(null, "Valeria Cruz", "20242TI015", BALCON, EstadoPedido.RECIBIDO, momento(11, 58, 0));
        linea(p10, menu, "Frappé", 1, "Cajeta", null);
        linea(p10, menu, "Sándwich", 1, "Panela", null);

        pedidos.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));
    }

    private Pedido pedido(Integer usuarioId, String cliente, String matricula, String cafeteria, EstadoPedido estado, long creadoEn) {
        Pedido p = new Pedido();
        p.setUsuarioId(usuarioId);
        p.setClienteNombre(cliente);
        p.setMatricula(matricula);
        p.setCafeteriaId(cafeteria);
        p.setEstado(estado);
        p.setCreadoEn(creadoEn);
        return p;
    }

    private void linea(Pedido pedido, List<Producto> menu, String nombre, int cantidad, String opcion, String nota) {
        Producto producto = menu.stream()
                .filter(x -> x.getCafeteriaId().equals(pedido.getCafeteriaId()) && x.getNombre().equals(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el platillo de ejemplo " + nombre));
        Opcion elegida = producto.getOpciones().stream()
                .filter(x -> x.getNombre().equals(opcion))
                .findFirst()
                .orElse(producto.getOpciones().isEmpty() ? null : producto.getOpciones().get(0));
        int extra = elegida == null ? 0 : elegida.getExtra();
        pedido.getLineas().add(new LineaPedido(producto, elegida == null ? null : elegida.getNombre(), nota,
                producto.getPrecio() + extra, cantidad));
    }

    private long momento(int hora, int minuto, int diasAtras) {
        return ZonedDateTime.now(zona).minusDays(diasAtras)
                .withHour(hora).withMinute(minuto).withSecond(0).withNano(0)
                .toInstant().toEpochMilli();
    }

    private static final class Plato {
        private final Producto p = new Producto();

        Plato(String cafeteria, Categoria categoria, String nombre, String descripcion, int precio) {
            p.setCafeteriaId(cafeteria);
            p.setCategoria(categoria);
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecio(precio);
            p.setCreadoEn(System.currentTimeMillis());
        }

        Plato dia(int dia) {
            p.setDia(dia);
            return this;
        }

        Plato opciones(String grupo, Opcion... opciones) {
            p.setGrupoOpciones(grupo);
            p.getOpciones().addAll(List.of(opciones));
            return this;
        }

        Plato detalle(String detalle) {
            p.setDetalle(detalle);
            return this;
        }

        Plato etiqueta(String etiqueta) {
            p.setEtiqueta(etiqueta);
            return this;
        }

        Plato foto(String foto) {
            p.setFoto(foto);
            return this;
        }

        Plato prep(int min, int max) {
            p.setPrepMin(min);
            p.setPrepMax(max);
            return this;
        }

        Plato recomendacion(String recomendacion) {
            p.setRecomendacion(recomendacion);
            return this;
        }

        Plato agotado() {
            p.setDisponible(false);
            return this;
        }
    }
}
