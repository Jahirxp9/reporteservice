package com.delgado.reporteservice.controller;

import com.delgado.reporteservice.entity.Reporte;
import com.delgado.reporteservice.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    @Autowired
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Reporte> registrarReporte(@Valid @RequestBody Reporte reporte) {
        Reporte nuevoReporte = reporteService.registrarReporte(reporte);
        return new ResponseEntity<>(nuevoReporte, HttpStatus.CREATED);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<Reporte>> obtenerReportesPaginados(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<Reporte> reportes = reporteService.obtenerReportesPaginados(offset, limit);
        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reporte> obtenerReportePorId(@PathVariable Integer id) {
        Reporte reporte = reporteService.obtenerReportePorId(id);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ciudadano/{dni}")
    public ResponseEntity<List<Reporte>> obtenerReportesPorDni(@PathVariable String dni) {
        List<Reporte> reportes = reporteService.obtenerReportesPorDni(dni);
        return ResponseEntity.ok(reportes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reporte> actualizarEstadoAtendido(@PathVariable Integer id) {
        Reporte reporteActualizado = reporteService.actualizarEstadoAtendido(id);
        return ResponseEntity.ok(reporteActualizado);
    }
}
