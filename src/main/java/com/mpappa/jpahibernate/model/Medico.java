package com.mpappa.jpahibernate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medico",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medico_colegiado", columnNames = "colegiado")
        })
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El número de colegiado no puede estar vacío")
    @Size(min = 5, max = 20, message = "El número de colegiado debe tener entre 5 y 20 caracteres")
    @Column(name = "colegiado", nullable = false, unique = true, length = 20)
    private String colegiado;

    @NotNull(message = "La especialidad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "especialidad", nullable = false)
    private Especialidad especialidad;

    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    @Column(name = "email", length = 100)
    private String email;

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cita> citas = new ArrayList<>();

    public Medico() {}

    public Medico(String nombre, String colegiado, Especialidad especialidad, String email) {
        this.nombre = nombre;
        this.colegiado = colegiado;
        this.especialidad = especialidad;
        this.email = email;
    }

    public void agregarCita(Cita cita) {
        citas.add(cita);
        cita.setMedico(this);
    }

    public void removerCita(Cita cita) {
        citas.remove(cita);
        cita.setMedico(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColegiado() {
        return colegiado;
    }

    public void setColegiado(String colegiado) {
        this.colegiado = colegiado;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }

    @Override
    public String toString() {
        return "Medico{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", colegiado='" + colegiado + '\'' +
                ", especialidad=" + especialidad +
                ", email='" + email + '\'' +
                '}';
    }
}