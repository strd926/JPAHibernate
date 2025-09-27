package com.mpappa.jpahibernate.util;

import com.mpappa.jpahibernate.model.*;
import com.mpappa.jpahibernate.service.HospitalService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class EntityManagerUtil {

    private static final Scanner scanner = new Scanner(System.in);
    private static final HospitalService service = new HospitalService();

    public static void main(String[] args) {
        System.out.println(" SISTEMA DE GESTIÓN HOSPITALARIA ");

        if (!JPAUtil.isInitialized()) {
            System.err.println("Error: No se pudo inicializar la conexión a la base de datos.");
            return;
        }

        boolean continuar = true;
        while (continuar) {
            mostrarMenuPrincipal();

            try {
                int opcion = Integer.parseInt(scanner.nextLine().trim());
                continuar = procesarOpcionMenu(opcion);
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingrese un número válido.\n");
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage() + "\n");
            }
        }

        cerrarAplicacion();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n==================== MENÚ PRINCIPAL ====================");
        System.out.println("1.  Registrar paciente");
        System.out.println("2.  Crear/editar historial médico de un paciente");
        System.out.println("3.  Registrar médico");
        System.out.println("4.  Agendar cita");
        System.out.println("5.  Cambiar estado de una cita");
        System.out.println("6.  Consultas");
        System.out.println("7.  Eliminar");
        System.out.println("8.  Semilla de datos (datos de prueba)");
        System.out.println("9.  Salir");
        System.out.println("========================================================");
        System.out.print("Seleccione una opción: ");
    }

    private static boolean procesarOpcionMenu(int opcion) {
        try {
            switch (opcion) {
                case 1:
                    registrarPaciente();
                    break;
                case 2:
                    gestionarHistorialMedico();
                    break;
                case 3:
                    registrarMedico();
                    break;
                case 4:
                    agendarCita();
                    break;
                case 5:
                    cambiarEstadoCita();
                    break;
                case 6:
                    mostrarMenuConsultas();
                    break;
                case 7:
                    mostrarMenuEliminar();
                    break;
                case 8:
                    insertarDatosPrueba();
                    break;
                case 9:
                    return false;
                default:
                    System.out.println("Opción no válida. Intente nuevamente.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
        return true;
    }

    private static void registrarPaciente() {
        System.out.println("\n--- REGISTRAR PACIENTE ---");

        try {
            System.out.print("Nombre completo: ");
            String nombre = scanner.nextLine().trim();

            System.out.print("DPI (13 dígitos): ");
            String dpi = scanner.nextLine().trim();

            if (!ValidationUtil.isValidDPI(dpi)) {
                System.out.println("Error: El DPI debe tener exactamente 13 dígitos.");
                return;
            }

            System.out.print("Fecha de nacimiento (dd/MM/yyyy): ");
            String fechaStr = scanner.nextLine().trim();
            LocalDate fechaNacimiento;

            try {
                fechaNacimiento = ValidationUtil.parseDate(fechaStr);
                if (!ValidationUtil.isValidAge(fechaNacimiento)) {
                    System.out.println("Error: Fecha de nacimiento no válida.");
                    return;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha incorrecto. Use dd/MM/yyyy");
                return;
            }

            System.out.print("Teléfono (opcional): ");
            String telefono = scanner.nextLine().trim();
            if (!ValidationUtil.isValidPhone(telefono)) {
                System.out.println("Error: Formato de teléfono no válido.");
                return;
            }

            System.out.print("Email (opcional): ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
                System.out.println("Error: Formato de email no válido.");
                return;
            }

            Paciente paciente = new Paciente(nombre, dpi, fechaNacimiento,
                    telefono.isEmpty() ? null : telefono,
                    email.isEmpty() ? null : email);

            Paciente pacienteRegistrado = service.registrarPaciente(paciente);
            System.out.println("✓ Paciente registrado exitosamente con ID: " + pacienteRegistrado.getId());

        } catch (Exception e) {
            System.out.println("Error al registrar paciente: " + e.getMessage());
        }
    }

    private static void gestionarHistorialMedico() {
        System.out.println("\n--- GESTIONAR HISTORIAL MÉDICO ---");

        try {
            Paciente paciente = seleccionarPaciente();
            if (paciente == null) return;

            HistorialMedico historialExistente = service.buscarHistorialMedicoPorPaciente(paciente.getId());

            if (historialExistente != null) {
                System.out.println("\nHistorial médico existente:");
                mostrarHistorialMedico(historialExistente);
                System.out.println("\n¿Desea actualizarlo? (s/n): ");
                if (!scanner.nextLine().trim().toLowerCase().startsWith("s")) {
                    return;
                }
            }

            System.out.print("Alergias: ");
            String alergias = scanner.nextLine().trim();

            System.out.print("Antecedentes: ");
            String antecedentes = scanner.nextLine().trim();

            System.out.print("Observaciones: ");
            String observaciones = scanner.nextLine().trim();

            HistorialMedico historial = service.crearOActualizarHistorialMedico(
                    paciente.getId(),
                    alergias.isEmpty() ? null : alergias,
                    antecedentes.isEmpty() ? null : antecedentes,
                    observaciones.isEmpty() ? null : observaciones
            );

            System.out.println("✓ Historial médico " +
                    (historialExistente != null ? "actualizado" : "creado") + " exitosamente.");

        } catch (Exception e) {
            System.out.println("Error al gestionar historial médico: " + e.getMessage());
        }
    }

    private static void registrarMedico() {
        System.out.println("\n--- REGISTRAR MÉDICO ---");

        try {
            System.out.print("Nombre completo: ");
            String nombre = scanner.nextLine().trim();

            System.out.print("Número de colegiado: ");
            String colegiado = scanner.nextLine().trim();

            System.out.println("Especialidades disponibles:");
            Especialidad[] especialidades = Especialidad.values();
            for (int i = 0; i < especialidades.length; i++) {
                System.out.println((i + 1) + ". " + especialidades[i].getDescripcion());
            }

            System.out.print("Seleccione especialidad (número): ");
            int especialidadIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (especialidadIndex < 0 || especialidadIndex >= especialidades.length) {
                System.out.println("Error: Especialidad no válida.");
                return;
            }

            System.out.print("Email (opcional): ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
                System.out.println("Error: Formato de email no válido.");
                return;
            }

            Medico medico = new Medico(nombre, colegiado, especialidades[especialidadIndex],
                    email.isEmpty() ? null : email);

            Medico medicoRegistrado = service.registrarMedico(medico);
            System.out.println("✓ Médico registrado exitosamente con ID: " + medicoRegistrado.getId());

        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese un número válido para la especialidad.");
        } catch (Exception e) {
            System.out.println("Error al registrar médico: " + e.getMessage());
        }
    }

    private static void agendarCita() {
        System.out.println("\n--- AGENDAR CITA ---");

        try {
            Paciente paciente = seleccionarPaciente();
            if (paciente == null) return;

            Medico medico = seleccionarMedico();
            if (medico == null) return;

            System.out.print("Fecha y hora (dd/MM/yyyy HH:mm): ");
            String fechaHoraStr = scanner.nextLine().trim();
            LocalDateTime fechaHora;

            try {
                fechaHora = ValidationUtil.parseDateTime(fechaHoraStr);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha y hora incorrecto. Use dd/MM/yyyy HH:mm");
                return;
            }

            if (!ValidationUtil.isFutureDateTime(fechaHora)) {
                System.out.println("Error: La fecha y hora deben ser futuras.");
                return;
            }

            System.out.print("Motivo de la consulta: ");
            String motivo = scanner.nextLine().trim();

            if (!ValidationUtil.isNotBlank(motivo)) {
                System.out.println("Error: El motivo no puede estar vacío.");
                return;
            }

            Cita cita = new Cita(fechaHora, motivo, paciente, medico);
            Cita citaAgendada = service.agendarCita(cita);
            System.out.println("✓ Cita agendada exitosamente con ID: " + citaAgendada.getId());

        } catch (Exception e) {
            System.out.println("Error al agendar cita: " + e.getMessage());
        }
    }

    private static void cambiarEstadoCita() {
        System.out.println("\n--- CAMBIAR ESTADO DE CITA ---");

        try {
            System.out.print("ID de la cita: ");
            Long citaId = Long.parseLong(scanner.nextLine().trim());

            System.out.println("Estados disponibles:");
            Estado[] estados = Estado.values();
            for (int i = 0; i < estados.length; i++) {
                System.out.println((i + 1) + ". " + estados[i].getDescripcion());
            }

            System.out.print("Seleccione nuevo estado (número): ");
            int estadoIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (estadoIndex < 0 || estadoIndex >= estados.length) {
                System.out.println("Error: Estado no válido.");
                return;
            }

            Cita citaActualizada = service.cambiarEstadoCita(citaId, estados[estadoIndex]);
            System.out.println("✓ Estado de cita actualizado: " + citaActualizada);

        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese un número válido.");
        } catch (Exception e) {
            System.out.println("Error al cambiar estado de cita: " + e.getMessage());
        }
    }

    private static void mostrarMenuConsultas() {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- MENÚ DE CONSULTAS ---");
            System.out.println("1. Listar todos los pacientes");
            System.out.println("2. Listar todos los médicos");
            System.out.println("3. Listar todas las citas");
            System.out.println("4. Buscar citas por paciente");
            System.out.println("5. Buscar próximas citas por médico");
            System.out.println("6. Buscar citas por rango de fechas");
            System.out.println("7. Volver al menú principal");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine().trim());
                switch (opcion) {
                    case 1:
                        listarPacientes();
                        break;
                    case 2:
                        listarMedicos();
                        break;
                    case 3:
                        listarTodasLasCitas();
                        break;
                    case 4:
                        buscarCitasPorPaciente();
                        break;
                    case 5:
                        buscarProximasCitasPorMedico();
                        break;
                    case 6:
                        buscarCitasPorRangoFechas();
                        break;
                    case 7:
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opción no válida. Intente nuevamente.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingrese un número válido.\n");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }
    }

    private static void mostrarMenuEliminar() {
        System.out.println("\n--- MENÚ ELIMINAR ---");
        System.out.println("1. Eliminar paciente");
        System.out.println("2. Eliminar cita");
        System.out.println("3. Volver al menú principal");
        System.out.print("Seleccione una opción: ");

        try {
            int opcion = Integer.parseInt(scanner.nextLine().trim());
            switch (opcion) {
                case 1:
                    eliminarPaciente();
                    break;
                case 2:
                    eliminarCita();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Opción no válida. Intente nuevamente.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese un número válido.\n");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private static void insertarDatosPrueba() {
        try {
            service.insertarDatosDePrueba();
            System.out.println("✓ Datos de prueba insertados exitosamente.");
        } catch (Exception e) {
            System.out.println("Error al insertar datos de prueba: " + e.getMessage());
        }
    }

    private static Paciente seleccionarPaciente() {
        System.out.print("\nDPI del paciente: ");
        String dpi = scanner.nextLine().trim();
        Paciente paciente = service.buscarPacientePorDpi(dpi);
        if (paciente == null) {
            System.out.println("Error: No se encontró paciente con DPI: " + dpi);
            return null;
        }
        System.out.println("Paciente seleccionado: " + paciente.getNombre());
        return paciente;
    }

    private static Medico seleccionarMedico() {
        System.out.print("\nNúmero de colegiado del médico: ");
        String colegiado = scanner.nextLine().trim();
        Medico medico = service.buscarMedicoPorColegiado(colegiado);
        if (medico == null) {
            System.out.println("Error: No se encontró médico con colegiado: " + colegiado);
            return null;
        }
        System.out.println("Médico seleccionado: " + medico.getNombre());
        return medico;
    }

    private static void mostrarHistorialMedico(HistorialMedico historial) {
        System.out.println("Alergias: " + (historial.getAlergias() != null ? historial.getAlergias() : "Ninguna"));
        System.out.println("Antecedentes: " + (historial.getAntecedentes() != null ? historial.getAntecedentes() : "Ninguno"));
        System.out.println("Observaciones: " + (historial.getObservaciones() != null ? historial.getObservaciones() : "Ninguna"));
    }

    private static void listarPacientes() {
        List<Paciente> pacientes = service.listarPacientes();
        System.out.println("\n--- LISTA DE PACIENTES ---");
        if (pacientes.isEmpty()) {
            System.out.println("No hay pacientes registrados.");
        } else {
            for (Paciente paciente : pacientes) {
                System.out.println(paciente);
                HistorialMedico historial = service.buscarHistorialMedicoPorPaciente(paciente.getId());
                if (historial != null) {
                    System.out.println("  Historial Médico:");
                    System.out.println("    Alergias: " + (historial.getAlergias() != null ? historial.getAlergias() : "Ninguna"));
                    System.out.println("    Antecedentes: " + (historial.getAntecedentes() != null ? historial.getAntecedentes() : "Ninguno"));
                    System.out.println("    Observaciones: " + (historial.getObservaciones() != null ? historial.getObservaciones() : "Ninguna"));
                } else {
                    System.out.println("  Historial Médico: No disponible");
                }
                System.out.println("-------------------------");
            }
        }
    }

    private static void listarMedicos() {
        List<Medico> medicos = service.listarMedicos();
        System.out.println("\n--- LISTA DE MÉDICOS ---");
        if (medicos.isEmpty()) {
            System.out.println("No hay médicos registrados.");
        } else {
            for (Medico medico : medicos) {
                System.out.println(medico);
            }
        }
    }

    private static void listarTodasLasCitas() {
        List<Cita> citas = service.listarTodasLasCitas();
        System.out.println("\n--- LISTA DE CITAS ---");
        if (citas.isEmpty()) {
            System.out.println("No hay citas registradas.");
        } else {
            for (Cita cita : citas) {
                System.out.println(cita);
            }
        }
    }

    private static void buscarCitasPorPaciente() {
        Paciente paciente = seleccionarPaciente();
        if (paciente == null) return;
        List<Cita> citas = service.listarCitasPorPaciente(paciente.getId());
        System.out.println("\n--- CITAS DEL PACIENTE ---");
        if (citas.isEmpty()) {
            System.out.println("No hay citas para este paciente.");
        } else {
            for (Cita cita : citas) {
                System.out.println(cita);
            }
        }
    }

    private static void buscarProximasCitasPorMedico() {
        Medico medico = seleccionarMedico();
        if (medico == null) return;
        List<Cita> citas = service.listarProximasCitasPorMedico(medico.getId());
        System.out.println("\n--- PRÓXIMAS CITAS DEL MÉDICO ---");
        if (citas.isEmpty()) {
            System.out.println("No hay citas próximas para este médico.");
        } else {
            for (Cita cita : citas) {
                System.out.println(cita);
            }
        }
    }

    private static void buscarCitasPorRangoFechas() {
        System.out.println("\n--- BUSCAR CITAS POR RANGO DE FECHAS ---");
        try {
            System.out.print("Fecha de inicio (dd/MM/yyyy): ");
            LocalDate fechaInicio = ValidationUtil.parseDate(scanner.nextLine().trim());
            System.out.print("Fecha de fin (dd/MM/yyyy): ");
            LocalDate fechaFin = ValidationUtil.parseDate(scanner.nextLine().trim());

            List<Cita> citas = service.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
            System.out.println("\n--- CITAS EN EL RANGO ---");
            if (citas.isEmpty()) {
                System.out.println("No hay citas en el rango de fechas especificado.");
            } else {
                for (Cita cita : citas) {
                    System.out.println(cita);
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Error: Formato de fecha incorrecto. Use dd/MM/yyyy");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void eliminarPaciente() {
        System.out.print("\nDPI del paciente a eliminar: ");
        String dpi = scanner.nextLine().trim();
        Paciente paciente = service.buscarPacientePorDpi(dpi);
        if (paciente == null) {
            System.out.println("Error: No se encontró paciente con DPI: " + dpi);
            return;
        }
        try {
            service.eliminarPaciente(paciente.getId());
            System.out.println("✓ Paciente eliminado exitosamente.");
        } catch (Exception e) {
            System.out.println("Error al eliminar paciente: " + e.getMessage());
        }
    }

    private static void eliminarCita() {
        System.out.print("\nID de la cita a eliminar: ");
        try {
            Long citaId = Long.parseLong(scanner.nextLine().trim());
            service.eliminarCita(citaId);
            System.out.println("✓ Cita eliminada exitosamente.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese un ID válido.");
        } catch (Exception e) {
            System.out.println("Error al eliminar cita: " + e.getMessage());
        }
    }

    private static void cerrarAplicacion() {
        JPAUtil.shutdown();
        scanner.close();
        System.out.println("Aplicación cerrada.");
    }
}