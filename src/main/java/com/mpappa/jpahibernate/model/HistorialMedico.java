package com.mpappa.jpahibernate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "historial_medico")
public class HistorialMedico {

    @Id
    @Column(name = "id")
    private Long id;

    @Size(max = 500, message = "Las alergias no pueden exceder los 500 caracteres")
    @Column(name = "alergias", length = 500)
    private String alergias;

    @Size(max = 1000, message = "Los antecedentes no pueden exceder los 1000 caracteres")
    @Column(name = "antecedentes", length = 1000)
    private String antecedentes;

    @Size(max = 2000, message = "Las observaciones no pueden exceder los 2000 caracteres")
    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Paciente paciente;

    public HistorialMedico() {}

    public HistorialMedico(Paciente paciente) {
        this.paciente = paciente;
        this.id = paciente.getId();
    }

    public HistorialMedico(Paciente paciente, String alergias, String antecedentes, String observaciones) {
        this.paciente = paciente;
        this.id = paciente.getId();
        this.alergias = alergias;
        this.antecedentes = antecedentes;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        if (paciente != null) {
            this.id = paciente.getId();
        }
    }

    @Override
    public String toString() {
        return "HistorialMedico{" +
                "id=" + id +
                ", alergias='" + alergias + '\'' +
                ", antecedentes='" + antecedentes + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}