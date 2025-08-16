package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username").trim();
        String email = req.getParameter("email").trim();
        String password = req.getParameter("password").trim();

        try (Connection con = new DatabaseConectivity().getConnection()) {
            String query = "SELECT user_id FROM users WHERE username = ? AND email = ? AND password = ? ";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                HttpSession session = req.getSession();
                session.setAttribute("admin_id", rs.getInt("user_id"));
                session.setAttribute("admin_username", username);
                resp.sendRedirect("AdminDashboard.html"); 
            } else {
                resp.sendRedirect("AdminLogin.html?error=Invalid admin credentials");
            }
        } catch (Exception e) {
            resp.sendRedirect("AdminLogin.html?error=Database error");
        }
    }
}
