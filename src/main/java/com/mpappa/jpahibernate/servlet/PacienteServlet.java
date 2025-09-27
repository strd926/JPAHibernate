package com.mpappa.jpahibernate.servlet;

import com.mpappa.jpahibernate.service.HospitalService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/pacientes")
public class PacienteServlet extends HttpServlet {
    private final HospitalService hospitalService = new HospitalService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var pacientes = hospitalService.listarPacientes();
            resp.setContentType("text/plain");
            resp.getWriter().write(pacientes.toString());
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}