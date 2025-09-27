package com.mpappa.jpahibernate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cita_medico_fecha", columnNames = {"medico_id", "fecha_hora"})
        },
        indexes = {
                @Index(name = "idx_cita_medico", columnList = "medico_id"),
                @Index(name = "idx_cita_paciente", columnList = "paciente_id"),
                @Index(name = "idx_cita_fecha_hora", columnList = "fecha_hora")
        })
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @NotBlank(message = "El motivo no puede estar vacío")
    @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @NotNull(message = "El paciente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @NotNull(message = "El médico es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    public Cita() {
        this.estado = Estado.PROGRAMADA;
    }

    public Cita(LocalDateTime fechaHora, String motivo, Paciente paciente, Medico medico) {
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.paciente = paciente;
        this.medico = medico;
        this.estado = Estado.PROGRAMADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado +
                ", motivo='" + motivo + '\'' +
                ", paciente=" + (paciente != null ? paciente.getNombre() : "null") +
                ", medico=" + (medico != null ? medico.getNombre() : "null") +
                '}';
    }
}