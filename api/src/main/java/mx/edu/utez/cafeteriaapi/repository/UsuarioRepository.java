package mx.edu.utez.cafeteriaapi.repository;

import mx.edu.utez.cafeteriaapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data arma las consultas a partir del nombre del método
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Para iniciar sesión con matrícula (alumno) o correo (encargado)
    Optional<Usuario> findFirstByMatriculaIgnoreCaseOrCorreoIgnoreCase(String matricula, String correo);

    boolean existsByMatriculaIgnoreCase(String matricula);
}
