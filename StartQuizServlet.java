package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/StartQuizServlet")
public class StartQuizServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("UserLogin.html");
            return;
        }

        int categoryId = Integer.parseInt(req.getParameter("category_id"));

        try (Connection con = new DatabaseConectivity().getConnection()) {
            String checkQuery = "SELECT * FROM results WHERE user_id = ? AND category_id = ?";
            PreparedStatement stmt = con.prepareStatement(checkQuery);
            stmt.setInt(1, (int) session.getAttribute("user_id"));
            stmt.setInt(2, categoryId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                resp.sendRedirect("CategoriesHtml.html?error=Test already taken for this category");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        session.setAttribute("category_id", categoryId);
        session.setAttribute("currentQuestion", 1);
        session.setAttribute("resetTimer", true);  
        session.removeAttribute("answers");

        resp.sendRedirect("Question");
    }
}
