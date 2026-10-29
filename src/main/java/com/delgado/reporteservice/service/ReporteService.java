package com.delgado.reporteservice.service;

import com.delgado.reporteservice.entity.Reporte;

import java.util.List;

public interface ReporteService {

    Reporte registrarReporte(Reporte reporte);

    List<Reporte> obtenerReportesPaginados(int offset, int limit);

    Reporte obtenerReportePorId(Integer id);

    List<Reporte> obtenerReportesPorDni(String dni);

    Reporte actualizarEstadoAtendido(Integer id);
}
