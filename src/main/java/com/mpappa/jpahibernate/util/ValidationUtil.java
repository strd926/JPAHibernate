package com.mpappa.jpahibernate.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidationUtil {

    public static boolean isValidDPI(String dpi) {
        return dpi != null && dpi.matches("\\d{13}");
    }

    public static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(dateStr, formatter);
    }

    public static boolean isValidAge(LocalDate fechaNacimiento) {
        return fechaNacimiento != null && fechaNacimiento.isBefore(LocalDate.now());
    }

    public static boolean isValidPhone(String phone) {
        return phone == null || phone.isEmpty() || phone.matches("\\d{8,20}");
    }

    public static boolean isValidEmail(String email) {
        return email == null || email.isEmpty() || email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    public static boolean isFutureDateTime(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}