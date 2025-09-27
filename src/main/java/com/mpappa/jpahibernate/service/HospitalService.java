package com.mpappa.jpahibernate.service;

import com.mpappa.jpahibernate.model.*;
import com.mpappa.jpahibernate.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class HospitalService {

    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    public <T> void validarEntidad(T entidad) throws IllegalArgumentException {
        Set<ConstraintViolation<T>> violaciones = validator.validate(entidad);
        if (!violaciones.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("Errores de validación:\n");
            for (ConstraintViolation<T> violacion : violaciones) {
                mensaje.append("- ").append(violacion.getMessage()).append("\n");
            }
            throw new IllegalArgumentException(mensaje.toString());
        }
    }

    public Paciente registrarPaciente(Paciente paciente) throws Exception {
        validarEntidad(paciente);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            if (existePacientePorDpi(paciente.getDpi())) {
                throw new IllegalArgumentException("Ya existe un paciente con el DPI: " + paciente.getDpi());
            }

            em.persist(paciente);
            tx.commit();

            return paciente;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean existePacientePorDpi(String dpi) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(p) FROM Paciente p WHERE p.dpi = :dpi", Long.class);
            query.setParameter("dpi", dpi);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public List<Paciente> listarPacientes() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Paciente> query = em.createQuery(
                    "SELECT p FROM Paciente p ORDER BY p.nombre", Paciente.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Paciente buscarPacientePorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Paciente.class, id);
        } finally {
            em.close();
        }
    }

    public Paciente buscarPacientePorDpi(String dpi) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Paciente> query = em.createQuery(
                    "SELECT p FROM Paciente p WHERE p.dpi = :dpi", Paciente.class);
            query.setParameter("dpi", dpi);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void eliminarPaciente(Long pacienteId) throws Exception {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Paciente paciente = em.find(Paciente.class, pacienteId);
            if (paciente == null) {
                throw new IllegalArgumentException("No se encontró el paciente con ID: " + pacienteId);
            }

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Cita c WHERE c.paciente.id = :pacienteId AND c.estado = :estado",
                    Long.class);
            query.setParameter("pacienteId", pacienteId);
            query.setParameter("estado", Estado.PROGRAMADA);

            if (query.getSingleResult() > 0) {
                throw new IllegalArgumentException(
                        "No se puede eliminar el paciente porque tiene citas programadas");
            }

            em.remove(paciente);
            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public HistorialMedico crearOActualizarHistorialMedico(Long pacienteId, String alergias,
                                                           String antecedentes, String observaciones) throws Exception {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Paciente paciente = em.find(Paciente.class, pacienteId);
            if (paciente == null) {
                throw new IllegalArgumentException("No se encontró el paciente con ID: " + pacienteId);
            }

            HistorialMedico historial = paciente.getHistorialMedico();

            if (historial == null) {
                historial = new HistorialMedico(paciente, alergias, antecedentes, observaciones);
                em.persist(historial);
                paciente.setHistorialMedico(historial);
            } else {
                historial.setAlergias(alergias);
                historial.setAntecedentes(antecedentes);
                historial.setObservaciones(observaciones);
                em.merge(historial);
            }

            tx.commit();
            return historial;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public HistorialMedico buscarHistorialMedicoPorPaciente(Long pacienteId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Paciente paciente = em.find(Paciente.class, pacienteId);
            return paciente != null ? paciente.getHistorialMedico() : null;
        } finally {
            em.close();
        }
    }

    public Medico registrarMedico(Medico medico) throws Exception {
        validarEntidad(medico);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            if (existeMedicoPorColegiado(medico.getColegiado())) {
                throw new IllegalArgumentException("Ya existe un médico con el número de colegiado: " +
                        medico.getColegiado());
            }

            em.persist(medico);
            tx.commit();

            return medico;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean existeMedicoPorColegiado(String colegiado) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(m) FROM Medico m WHERE m.colegiado = :colegiado", Long.class);
            query.setParameter("colegiado", colegiado);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public Medico buscarMedicoPorColegiado(String colegiado) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Medico> query = em.createQuery(
                    "SELECT m FROM Medico m WHERE m.colegiado = :colegiado", Medico.class);
            query.setParameter("colegiado", colegiado);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Medico> listarMedicos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Medico> query = em.createQuery(
                    "SELECT m FROM Medico m ORDER BY m.nombre", Medico.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Medico buscarMedicoPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Medico.class, id);
        } finally {
            em.close();
        }
    }

    public Cita agendarCita(Cita cita) throws Exception {
        validarEntidad(cita);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            if (!cita.getFechaHora().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("La fecha y hora de la cita debe ser futura");
            }

            if (existeCitaMedicoEnFecha(cita.getMedico().getId(), cita.getFechaHora())) {
                throw new IllegalArgumentException("El médico ya tiene una cita programada en esa fecha y hora");
            }

            em.persist(cita);
            tx.commit();

            return cita;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean existeCitaMedicoEnFecha(Long medicoId, LocalDateTime fechaHora) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Cita c WHERE c.medico.id = :medicoId AND c.fechaHora = :fechaHora " +
                            "AND c.estado != :estadoCancelada", Long.class);
            query.setParameter("medicoId", medicoId);
            query.setParameter("fechaHora", fechaHora);
            query.setParameter("estadoCancelada", Estado.CANCELADA);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public Cita cambiarEstadoCita(Long citaId, Estado nuevoEstado) throws Exception {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<Cita> query = em.createQuery(
                    "SELECT c FROM Cita c JOIN FETCH c.paciente JOIN FETCH c.medico WHERE c.id = :citaId",
                    Cita.class
            );
            query.setParameter("citaId", citaId);
            Cita cita = query.getSingleResult();

            if (cita == null) {
                throw new IllegalArgumentException("No se encontró la cita con ID: " + citaId);
            }

            cita.setEstado(nuevoEstado);
            em.merge(cita);
            tx.commit();

            return cita;

        } catch (NoResultException e) {
            throw new IllegalArgumentException("No se encontró la cita con ID: " + citaId);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Cita> listarCitasPorPaciente(Long pacienteId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Cita> query = em.createQuery(
                    "SELECT c FROM Cita c JOIN FETCH c.paciente JOIN FETCH c.medico WHERE c.paciente.id = :pacienteId ORDER BY c.fechaHora DESC",
                    Cita.class
            );
            query.setParameter("pacienteId", pacienteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> listarProximasCitasPorMedico(Long medicoId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Cita> query = em.createQuery(
                    "SELECT c FROM Cita c JOIN FETCH c.paciente JOIN FETCH c.medico WHERE c.medico.id = :medicoId AND c.fechaHora > :ahora AND c.estado = :estado ORDER BY c.fechaHora",
                    Cita.class
            );
            query.setParameter("medicoId", medicoId);
            query.setParameter("ahora", LocalDateTime.now());
            query.setParameter("estado", Estado.PROGRAMADA);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> buscarCitasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
            LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);

            TypedQuery<Cita> query = em.createQuery(
                    "SELECT c FROM Cita c JOIN FETCH c.paciente JOIN FETCH c.medico " +
                            "WHERE c.fechaHora BETWEEN :inicio AND :fin ORDER BY c.fechaHora", Cita.class);
            query.setParameter("inicio", inicioDateTime);
            query.setParameter("fin", finDateTime);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> listarTodasLasCitas() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Cita> query = em.createQuery(
                    "SELECT c FROM Cita c JOIN FETCH c.paciente JOIN FETCH c.medico ORDER BY c.fechaHora DESC",
                    Cita.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void eliminarCita(Long citaId) throws Exception {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Cita cita = em.find(Cita.class, citaId);
            if (cita == null) {
                throw new IllegalArgumentException("No se encontró la cita con ID: " + citaId);
            }

            em.remove(cita);
            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void insertarDatosDePrueba() throws Exception {
        Paciente paciente1 = new Paciente("Juan Pérez", "1234567890123",
                LocalDate.of(1985, 3, 15), "12345678", "juan.perez@email.com");
        Paciente paciente2 = new Paciente("María García", "9876543210987",
                LocalDate.of(1990, 7, 22), "87654321", "maria.garcia@email.com");

        registrarPaciente(paciente1);
        registrarPaciente(paciente2);

        Medico medico1 = new Medico("Dr. Carlos López", "MED001",
                Especialidad.CARDIOLOGIA, "carlos.lopez@hospital.com");
        Medico medico2 = new Medico("Dra. Ana Martínez", "MED002",
                Especialidad.PEDIATRIA, "ana.martinez@hospital.com");

        registrarMedico(medico1);
        registrarMedico(medico2);

        crearOActualizarHistorialMedico(paciente1.getId(), "Alergia al polen",
                "Hipertensión familiar", "Paciente estable");
        crearOActualizarHistorialMedico(paciente2.getId(), "Sin alergias conocidas",
                "Sin antecedentes relevantes", "Paciente sana");

        Cita cita1 = new Cita(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0),
                "Control rutinario", paciente1, medico1);
        Cita cita2 = new Cita(LocalDateTime.now().plusDays(14).withHour(15).withMinute(30),
                "Consulta pediátrica", paciente2, medico2);

        agendarCita(cita1);
        agendarCita(cita2);

        System.out.println("Datos de prueba insertados exitosamente.");
    }
}