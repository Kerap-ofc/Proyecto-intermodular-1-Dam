package com.example.proyecto.repository;

import com.example.proyecto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    // Consultas simplificadas para asegurar que no fallen por nombres de columnas del recurso
    @Query(value = "SELECT * FROM book_reservations WHERE user_id = :userId", nativeQuery = true)
    List<Map<String, Object>> findBookReservationsByUserId(@Param("userId") Integer userId);

    @Query(value = "SELECT * FROM music_reservations WHERE user_id = :userId", nativeQuery = true)
    List<Map<String, Object>> findMusicReservationsByUserId(@Param("userId") Integer userId);
}