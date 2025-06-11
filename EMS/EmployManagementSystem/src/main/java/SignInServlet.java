import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/v1/signin")
public class SignInServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> user = mapper.readValue(req.getReader(), Map.class);

        String email = user.get("email");
        String password = user.get("password");
        ServletContext context = req.getServletContext();
        BasicDataSource ds = (BasicDataSource) context.getAttribute("dataSource");

        try {
            Connection conn = ds.getConnection();
            String checkEmailSQL = "SELECT password FROM users WHERE email = ?";
            try {
                PreparedStatement stmt = conn.prepareStatement(checkEmailSQL);
                stmt.setString(1, email);
                var rs = stmt.executeQuery();

                if (!rs.next()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"message\": \"User not found.\"}");
                } else {
                    String correctPassword = rs.getString("password");
                    if (!correctPassword.equals(password)) {
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        resp.getWriter().write("{\"message\": \"Incorrect password.\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("{\"message\": \"Login successful!\"}");
                    }
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"message\": \"Database error: " + e.getMessage().replace("\"", "'") + "\"}");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
