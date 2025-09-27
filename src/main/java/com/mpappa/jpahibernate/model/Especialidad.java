package com.mpappa.jpahibernate.model;

public enum Especialidad {
    CARDIOLOGIA("Cardiología"),
    NEUROLOGIA("Neurología"),
    PEDIATRIA("Pediatría"),
    GINECOLOGIA("Ginecología"),
    TRAUMATOLOGIA("Traumatología"),
    MEDICINA_GENERAL("Medicina General"),
    DERMATOLOGIA("Dermatología"),
    OFTALMOLOGIA("Oftalmología"),
    PSIQUIATRIA("Psiquiatría"),
    ONCOLOGIA("Oncología");

    private final String descripcion;

    Especialidad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
