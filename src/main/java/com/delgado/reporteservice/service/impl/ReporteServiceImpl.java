package com.delgado.reporteservice.service.impl;

import com.delgado.reporteservice.entity.Reporte;
import com.delgado.reporteservice.exception.ResourceNotFoundException;
import com.delgado.reporteservice.repository.ReporteRepository;
import com.delgado.reporteservice.service.ReporteService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final ReporteRepository reporteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ReporteServiceImpl(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Override
    @Transactional
    public Reporte registrarReporte(Reporte reporte) {
        if (reporte.getDni() == null || !reporte.getDni().matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos");
        }
        reporte.setId(null);
        reporte.setFecha(LocalDateTime.now());
        reporte.setEstado(1);
        return reporteRepository.save(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reporte> obtenerReportesPaginados(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("El offset no puede ser negativo");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("El límite debe ser mayor a cero");
        }
        return entityManager.createQuery("SELECT r FROM Reporte r ORDER BY r.id ASC", Reporte.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Reporte obtenerReportePorId(Integer id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reporte> obtenerReportesPorDni(String dni) {
        if (dni == null || !dni.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos");
        }
        return reporteRepository.findByDniOrderByIdAsc(dni);
    }

    @Override
    @Transactional
    public Reporte actualizarEstadoAtendido(Integer id) {
        Reporte reporte = obtenerReportePorId(id);
        reporte.setEstado(0);
        return reporteRepository.save(reporte);
    }
}
