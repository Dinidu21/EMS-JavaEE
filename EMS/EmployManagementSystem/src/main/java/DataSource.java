import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLException;

@WebListener
public class DataSource implements ServletContextListener {
    @Override
    public void contextInitialized(jakarta.servlet.ServletContextEvent sce) {
        // Initialize the data source here
        System.out.println("Data source initialized.");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:3306/ems");
        ds.setUsername("root");
        ds.setPassword("root123");
        ds.setInitialSize(50);
        ds.setMaxTotal(100);

        ServletContext sc = sce.getServletContext();
        sc.setAttribute("dataSource", ds);
    }

    @Override
    public void contextDestroyed(jakarta.servlet.ServletContextEvent sce) {
        // Clean up the data source here
        System.out.println("Data source destroyed.");
        try {
            ServletContext sc = sce.getServletContext();
            BasicDataSource ds = (BasicDataSource) sc.getAttribute("dataSource");
            ds.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
