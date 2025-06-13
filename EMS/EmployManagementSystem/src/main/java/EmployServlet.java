import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Employee;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/employees/*")
public class EmployServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        ServletContext context = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) context.getAttribute("dataSource");

        String pathInfo = req.getPathInfo();
        try (Connection conn = ds.getConnection()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all employees
                List<Employee> employees = new ArrayList<>();
                String sql = "SELECT * FROM employees";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        employees.add(new Employee(
                                rs.getInt("id"),
                                rs.getString("photo"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("role"),
                                rs.getString("address"),
                                rs.getString("status"),
                                rs.getString("created_at"),
                                rs.getString("updated_at")
                        ));
                    }
                    mapper.writeValue(resp.getWriter(), employees);
                }
            } else {
                // Get single employee by ID
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    String sql = "SELECT * FROM employees WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, id);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                Employee employee = new Employee(
                                        rs.getInt("id"),
                                        rs.getString("photo"),
                                        rs.getString("name"),
                                        rs.getString("email"),
                                        rs.getString("role"),
                                        rs.getString("address"),
                                        rs.getString("status"),
                                        rs.getString("created_at"),
                                        rs.getString("updated_at")
                                );
                                mapper.writeValue(resp.getWriter(), employee);
                            } else {
                                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                resp.getWriter().write("{\"message\": \"Employee not found.\"}");
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"message\": \"Invalid ID format.\"}");
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\": \"Database error: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        ServletContext context = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) context.getAttribute("dataSource");

        try {
            Map<String, String> employeeData = mapper.readValue(req.getReader(), Map.class);
            String name = employeeData.get("name");
            String email = employeeData.get("email");
            String role = employeeData.get("role");
            String address = employeeData.get("address");
            String status = employeeData.get("status");
            String photo = employeeData.get("photo");

            try (Connection conn = ds.getConnection()) {
                String sql = "INSERT INTO employees (photo, name, email, role, address, status) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, photo);
                    stmt.setString(2, name);
                    stmt.setString(3, email);
                    stmt.setString(4, role);
                    stmt.setString(5, address);
                    stmt.setString(6, status);
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int id = rs.getInt(1);
                            String selectSql = "SELECT * FROM employees WHERE id = ?";
                            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                                selectStmt.setInt(1, id);
                                try (ResultSet selectRs = selectStmt.executeQuery()) {
                                    if (selectRs.next()) {
                                        Employee newEmployee = new Employee(
                                                selectRs.getInt("id"),
                                                selectRs.getString("photo"),
                                                selectRs.getString("name"),
                                                selectRs.getString("email"),
                                                selectRs.getString("role"),
                                                selectRs.getString("address"),
                                                selectRs.getString("status"),
                                                selectRs.getString("created_at"),
                                                selectRs.getString("updated_at")
                                        );
                                        resp.setStatus(HttpServletResponse.SC_CREATED);
                                        mapper.writeValue(resp.getWriter(), newEmployee);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"message\": \"Database error: " + e.getMessage().replace("\"", "'") + "\"}");
            }
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\": \"Invalid JSON format.\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        ServletContext context = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) context.getAttribute("dataSource");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\": \"Employee ID required.\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Map<String, String> employeeData = mapper.readValue(req.getReader(), Map.class);
            String name = employeeData.get("name");
            String email = employeeData.get("email");
            String role = employeeData.get("role");
            String address = employeeData.get("address");
            String status = employeeData.get("status");
            String photo = employeeData.get("photo");

            try (Connection conn = ds.getConnection()) {
                String sql = "UPDATE employees SET photo = ?, name = ?, email = ?, role = ?, address = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, photo);
                    stmt.setString(2, name);
                    stmt.setString(3, email);
                    stmt.setString(4, role);
                    stmt.setString(5, address);
                    stmt.setString(6, status);
                    stmt.setInt(7, id);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        String selectSql = "SELECT * FROM employees WHERE id = ?";
                        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                            selectStmt.setInt(1, id);
                            try (ResultSet rs = selectStmt.executeQuery()) {
                                if (rs.next()) {
                                    Employee updatedEmployee = new Employee(
                                            rs.getInt("id"),
                                            rs.getString("photo"),
                                            rs.getString("name"),
                                            rs.getString("email"),
                                            rs.getString("role"),
                                            rs.getString("address"),
                                            rs.getString("status"),
                                            rs.getString("created_at"),
                                            rs.getString("updated_at")
                                    );
                                    mapper.writeValue(resp.getWriter(), updatedEmployee);
                                }
                            }
                        }
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"message\": \"Employee not found.\"}");
                    }
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"message\": \"Database error: " + e.getMessage().replace("\"", "'") + "\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\": \"Invalid ID format.\"}");
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\": \"Invalid JSON format.\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ServletContext context = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) context.getAttribute("dataSource");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\": \"Employee ID required.\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            try (Connection conn = ds.getConnection()) {
                String sql = "DELETE FROM employees WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"message\": \"Employee not found.\"}");
                    }
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"message\": \"Database error: " + e.getMessage().replace("\"", "'") + "\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\": \"Invalid ID format.\"}");
        }
    }
}