package mx.edu.utez.cafeteriaapi.repository;

import mx.edu.utez.cafeteriaapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findFirstByMatriculaIgnoreCaseOrCorreoIgnoreCase(String matricula, String correo);

    boolean existsByMatriculaIgnoreCase(String matricula);
}
