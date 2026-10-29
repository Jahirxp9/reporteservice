package com.delgado.reporteservice;

import com.delgado.reporteservice.controller.ReporteController;
import com.delgado.reporteservice.entity.Reporte;
import com.delgado.reporteservice.exception.GlobalExceptionHandler;
import com.delgado.reporteservice.repository.ReporteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class ReporteserviceApplicationTests {

	@Autowired
	private ReporteController reporteController;

	@Autowired
	private ReporteRepository reporteRepository;

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		reporteRepository.deleteAll();
		mockMvc = MockMvcBuilders.standaloneSetup(reporteController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void contextLoads() {
		assertNotNull(reporteRepository);
		assertNotNull(reporteController);
	}

	// 1. Registro válido
	@Test
	void testRegistroValido() throws Exception {
		Reporte reporte = new Reporte(null, "12345678", null, "Basura acumulada", "Av. Peru 123", "Cerca al parque", null);

		mockMvc.perform(post("/reportes/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reporte)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.dni", is("12345678")))
				.andExpect(jsonPath("$.tipo", is("Basura acumulada")))
				.andExpect(jsonPath("$.direccion", is("Av. Peru 123")))
				.andExpect(jsonPath("$.referencia", is("Cerca al parque")))
				.andExpect(jsonPath("$.estado", is(1)))
				.andExpect(jsonPath("$.fecha", notNullValue()));
	}

	// 2. Registro con DNI inválido
	@Test
	void testRegistroDniInvalido() throws Exception {
		Reporte reporte = new Reporte(null, "1234", null, "Basura acumulada", "Av. Peru 123", "Ref", 1);

		mockMvc.perform(post("/reportes/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reporte)))
				.andExpect(status().isBadRequest());
	}

	// 3. Campos obligatorios
	@Test
	void testCamposObligatorios() throws Exception {
		Reporte reporteSinTipo = new Reporte(null, "87654321", null, "", "Av. Peru 123", "Ref", 1);

		mockMvc.perform(post("/reportes/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reporteSinTipo)))
				.andExpect(status().isBadRequest());

		Reporte reporteSinDireccion = new Reporte(null, "87654321", null, "Acumulacion", "   ", "Ref", 1);

		mockMvc.perform(post("/reportes/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reporteSinDireccion)))
				.andExpect(status().isBadRequest());
	}

	// 4. Paginación con offset y limit
	@Test
	void testPaginacionOffsetLimit() throws Exception {
		for (int i = 1; i <= 7; i++) {
			Reporte r = new Reporte(null, "1000000" + i, LocalDateTime.now(), "Tipo " + i, "Direccion " + i, "Ref " + i, 1);
			reporteRepository.save(r);
		}

		mockMvc.perform(get("/reportes/").param("offset", "0").param("limit", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(5)));

		mockMvc.perform(get("/reportes/").param("offset", "5").param("limit", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	// 5. Consulta por ID
	@Test
	void testConsultaPorId() throws Exception {
		Reporte r = reporteRepository.save(new Reporte(null, "88888888", LocalDateTime.now(), "Residuo", "Calle 1", "Esquina", 1));

		mockMvc.perform(get("/reportes/" + r.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(r.getId())))
				.andExpect(jsonPath("$.dni", is("88888888")));
	}

	// 6. Reporte inexistente
	@Test
	void testReporteInexistente() throws Exception {
		mockMvc.perform(get("/reportes/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)));
	}

	// 7. Búsqueda por DNI
	@Test
	void testBusquedaPorDni() throws Exception {
		String dniTest = "77777777";
		reporteRepository.save(new Reporte(null, dniTest, LocalDateTime.now(), "Tipo A", "Dir A", "Ref A", 1));
		reporteRepository.save(new Reporte(null, dniTest, LocalDateTime.now(), "Tipo B", "Dir B", "Ref B", 1));
		reporteRepository.save(new Reporte(null, "11112222", LocalDateTime.now(), "Tipo C", "Dir C", "Ref C", 1));

		mockMvc.perform(get("/reportes/ciudadano/" + dniTest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].dni", is(dniTest)))
				.andExpect(jsonPath("$[1].dni", is(dniTest)));

		// DNI inválido
		mockMvc.perform(get("/reportes/ciudadano/123"))
				.andExpect(status().isBadRequest());
	}

	// 8. Actualización de estado
	@Test
	void testActualizacionEstado() throws Exception {
		Reporte r = reporteRepository.save(new Reporte(null, "55555555", LocalDateTime.now(), "Basura", "Calle 2", "Parque", 1));
		assertEquals(1, r.getEstado());

		mockMvc.perform(put("/reportes/" + r.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(r.getId())))
				.andExpect(jsonPath("$.estado", is(0)))
				.andExpect(jsonPath("$.dni", is("55555555")))
				.andExpect(jsonPath("$.tipo", is("Basura")));

		Reporte actualizado = reporteRepository.findById(r.getId()).orElseThrow();
		assertEquals(0, actualizado.getEstado());
	}
}
