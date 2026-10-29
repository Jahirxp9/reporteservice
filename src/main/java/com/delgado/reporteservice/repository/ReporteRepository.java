package com.delgado.reporteservice.repository;

import com.delgado.reporteservice.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findByDniOrderByIdAsc(String dni);
}
