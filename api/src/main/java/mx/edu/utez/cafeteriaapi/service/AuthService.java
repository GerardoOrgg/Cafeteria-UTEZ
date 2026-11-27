package mx.edu.utez.cafeteriaapi.service;

import mx.edu.utez.cafeteriaapi.dto.Peticiones.LoginRequest;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.RegistroRequest;
import mx.edu.utez.cafeteriaapi.dto.Peticiones.RestablecerRequest;
import mx.edu.utez.cafeteriaapi.exception.ApiException;
import mx.edu.utez.cafeteriaapi.model.Rol;
import mx.edu.utez.cafeteriaapi.model.Usuario;
import mx.edu.utez.cafeteriaapi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Inicio de sesión, registro de alumnos y permisos de encargado.
 */
@Service
public class AuthService {

    public static final String DOMINIO_CORREO = "utez.edu.mx";

    private final UsuarioRepository usuarios;

    public AuthService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    // Las contraseñas se guardan como SHA-256, nunca en texto plano
    public static String hash(String texto) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public Usuario iniciarSesion(LoginRequest datos) {
        Rol rol = leerRol(datos.rol());
        String usuario = datos.usuario().trim();
        Usuario u = usuarios.findFirstByMatriculaIgnoreCaseOrCorreoIgnoreCase(usuario, usuario)
                .orElseThrow(() -> ApiException.noAutorizado(rol == Rol.ALUMNO
                        ? "No existe una cuenta con esa matrícula o correo. Regístrate primero."
                        : "No existe esa cuenta de encargado."));
        if (!u.getContrasenaHash().equals(hash(datos.contrasena()))) {
            throw ApiException.noAutorizado("Contraseña incorrecta.");
        }
        // La pestaña que eligió en la app (Estudiante / Encargado) tiene que coincidir con su cuenta
        if (u.getRol() != rol) {
            throw ApiException.prohibido(rol == Rol.ENCARGADO
                    ? "Esta cuenta es de estudiante. Entra en la pestaña Estudiante."
                    : "Esta cuenta es de encargado. Entra en la pestaña Encargado.");
        }
        return u;
    }

    @Transactional
    public Usuario registrarAlumno(RegistroRequest datos) {
        String matricula = datos.matricula().trim().toUpperCase();
        if (usuarios.existsByMatriculaIgnoreCase(matricula)) {
            throw ApiException.conflicto("Esa matrícula ya tiene cuenta. Inicia sesión.");
        }
        Usuario nuevo = new Usuario(
                datos.nombre().trim(),
                matricula,
                matricula.toLowerCase() + "@" + DOMINIO_CORREO,
                hash(datos.contrasena()),
                Rol.ALUMNO,
                null
        );
        return usuarios.save(nuevo);
    }

    // Cambiar contraseña olvidada: se verifica con la matrícula y el nombre registrado
    @Transactional
    public void restablecer(RestablecerRequest datos) {
        Usuario u = usuarios.findFirstByMatriculaIgnoreCaseOrCorreoIgnoreCase(datos.matricula().trim(), datos.matricula().trim())
                .filter(x -> x.getRol() == Rol.ALUMNO && x.getNombre().equalsIgnoreCase(datos.nombre().trim()))
                .orElseThrow(() -> ApiException.invalido("La matrícula y el nombre no coinciden con ninguna cuenta."));
        u.setContrasenaHash(hash(datos.nueva()));
        usuarios.save(u);
    }

    /**
     * Revisa que quien hace la acción sea encargado y, si se indica, de esa cafetería.
     * La app manda el id del usuario en el encabezado X-Usuario-Id.
     */
    public Usuario exigirEncargado(Integer usuarioId, String cafeteriaId) {
        Usuario u = usuarios.findById(usuarioId)
                .orElseThrow(() -> ApiException.noAutorizado("Tu sesión ya no es válida. Vuelve a iniciar sesión."));
        if (u.getRol() != Rol.ENCARGADO) {
            throw ApiException.prohibido("Solo un encargado puede hacer esto.");
        }
        if (cafeteriaId != null && !cafeteriaId.equals(u.getCafeteriaId())) {
            throw ApiException.prohibido("Solo el encargado de esta cafetería puede hacer esto.");
        }
        return u;
    }

    private Rol leerRol(String texto) {
        try {
            return Rol.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.invalido("Tipo de cuenta desconocido: " + texto);
        }
    }
}
