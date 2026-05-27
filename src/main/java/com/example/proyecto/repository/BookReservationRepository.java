package com.example.proyecto.repository;

import com.example.proyecto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookReservationRepository extends JpaRepository<User, Integer> {
    // Aquí no hace falta escribir nada más, JpaRepository ya hace toda la magia
}