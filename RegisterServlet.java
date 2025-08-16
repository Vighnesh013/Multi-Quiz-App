package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/userRegister")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username").trim();
        String email = req.getParameter("email").trim();
        String password = req.getParameter("password").trim();

       
        if (username.isEmpty() || !username.matches("[a-zA-Z ]+")) {
            resp.sendRedirect("register.html?error=Invalid%20username.%20Only%20letters%20and%20spaces%20allowed.");
            return;
        }

        try (Connection con = new DatabaseConectivity().getConnection()) {

            var checkStmt = con.prepareStatement("SELECT user_id FROM users WHERE email=? OR username=?");
            checkStmt.setString(1, email);
            checkStmt.setString(2, username);
            var rs = checkStmt.executeQuery();

            if (rs.next()) {
                resp.sendRedirect("register.html?error=User already exists");
                return;
            }

            var stmt = con.prepareStatement(
                "INSERT INTO users (username, email, password, is_admin) VALUES (?, ?, ?, false)"
            );
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                resp.sendRedirect("UserLogin.html?success=Registration successful, please login");
            } else {
                resp.sendRedirect("register.html?error=Registration failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage().replaceAll(" ", "%20");
            resp.sendRedirect("register.html?error=" + message);
        }
    }
}
