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

    @Query(value = "SELECT b.genre AS genre, b.author AS author, b.title AS title, " +
                   "b.release_year AS release_year, b.description AS description, br.reservation_date AS reservation_date " +
                   "FROM books b JOIN book_reservations br ON b.id = br.book_id " +
                   "WHERE br.user_id = :userId AND LOWER(b.title) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
                   "ORDER BY " +
                   "CASE WHEN :orden = 'asc' THEN br.reservation_date END ASC, " +
                   "CASE WHEN :orden = 'desc' THEN br.reservation_date END DESC", nativeQuery = true)
    List<Map<String, Object>> findBookReservations(
            @Param("userId") Integer userId, 
            @Param("buscar") String buscar, 
            @Param("orden") String orden);

    // 2. CONSULTA PARA MÚSICA DEL USUARIO (CON BUSCADOR Y ORDENACIÓN)
    @Query(value = "SELECT m.artist_or_band AS artist_or_band, m.title AS title, " +
                   "m.genre AS genre, m.release_year AS release_year, mr.reservation_date AS reservation_date " +
                   "FROM music m JOIN music_reservations mr ON m.id = mr.music_id " +
                   "WHERE mr.user_id = :userId AND LOWER(m.title) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
                   "ORDER BY " +
                   "CASE WHEN :orden = 'asc' THEN mr.reservation_date END ASC, " +
                   "CASE WHEN :orden = 'desc' THEN mr.reservation_date END DESC", nativeQuery = true)
    List<Map<String, Object>> findMusicReservations(
            @Param("userId") Integer userId, 
            @Param("buscar") String buscar, 
            @Param("orden") String orden);
}