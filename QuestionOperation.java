package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/QuestionServlet")
public class QuestionOperation extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String categoryIdStr = req.getParameter("category_id");
        if (categoryIdStr == null) {
            resp.sendRedirect("AdminDashboard.html?error=Category ID required to view questions");
            return;
        }

        int categoryId = Integer.parseInt(categoryIdStr);

        try (Connection con = new DatabaseConectivity().getConnection()) {
            String query = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE category_id = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Questions for Category " + categoryId + "</title>");
            out.println("<link rel='stylesheet' href='Style.css' />"); 
            out.println("</head>");
            out.println("<body>");
            out.println("<main class='container'>");
            out.println("<h3>Questions for Category ID: " + categoryId + "</h3>");
            out.println("<table class='questions-table'>");
            out.println("<thead>");
            out.println("<tr><th>ID</th><th>Question Text</th><th>Option A</th><th>Option B</th><th>Option C</th><th>Option D</th><th>Correct Option</th><th>Action</th></tr>");
            out.println("</thead>");
            out.println("<tbody>");

            boolean hasQuestions = false;
            while (rs.next()) {
                hasQuestions = true;
                out.println("<tr>");
                out.println("<td>" + rs.getInt("question_id") + "</td>");
                out.println("<td>" + rs.getString("question_text") + "</td>");
                out.println("<td>" + rs.getString("option_a") + "</td>");
                out.println("<td>" + rs.getString("option_b") + "</td>");
                out.println("<td>" + rs.getString("option_c") + "</td>");
                out.println("<td>" + rs.getString("option_d") + "</td>");
                out.println("<td>" + rs.getString("correct_option") + "</td>");
                out.println("<td>");
                out.println("<form action='QuestionServlet' method='post' onsubmit='return confirm(\"Are you sure you want to delete this question?\");'>");
                out.println("<input type='hidden' name='action' value='delete' />");
                out.println("<input type='hidden' name='question_id' value='" + rs.getInt("question_id") + "' />");
                out.println("<input type='submit' value='Delete' class='btn btn-logout' />");
                out.println("</form>");
                out.println("</td>");
                out.println("</tr>");
            }

            if (!hasQuestions) {
                out.println("<tr><td colspan='8' class='no-data'>No questions found for this category.</td></tr>");
            }

            out.println("</tbody>");
            out.println("</table>");

            out.println("<a href='AdminDashboard.html' class='btn btn-primary'>Back to Dashboard</a>");

            out.println("</main>");
            out.println("</body>");
            out.println("</html>");
            out.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            resp.sendRedirect("AdminDashboard.html?error=Error fetching questions");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if (action == null) {
            resp.sendRedirect("AdminDashboard.html?error=Action required");
            return;
        }

        try (Connection con = new DatabaseConectivity().getConnection()) {
            if ("add".equalsIgnoreCase(action)) {

                String categoryIdStr = req.getParameter("category_id");
                String questionText = req.getParameter("question_text");
                String optionA = req.getParameter("option_a");
                String optionB = req.getParameter("option_b");
                String optionC = req.getParameter("option_c");
                String optionD = req.getParameter("option_d");
                String correctOption = req.getParameter("correct_option");

                if (categoryIdStr == null || questionText == null || optionA == null || optionB == null || optionC == null
                        || optionD == null || correctOption == null || questionText.trim().isEmpty() || optionA.trim().isEmpty()
                        || optionB.trim().isEmpty() || optionC.trim().isEmpty() || optionD.trim().isEmpty() || correctOption.trim().isEmpty()) {
                    resp.sendRedirect("AdminDashboard.html?error=All question fields are required");
                    return;
                }

                int categoryId = Integer.parseInt(categoryIdStr);
                correctOption = correctOption.toUpperCase();

                if (!correctOption.matches("[ABCD]")) {
                    resp.sendRedirect("AdminDashboard.html?error=Correct option must be A, B, C, or D");
                    return;
                }

                String insertQuery = "INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = con.prepareStatement(insertQuery);
                stmt.setInt(1, categoryId);
                stmt.setString(2, questionText.trim());
                stmt.setString(3, optionA.trim());
                stmt.setString(4, optionB.trim());
                stmt.setString(5, optionC.trim());
                stmt.setString(6, optionD.trim());
                stmt.setString(7, correctOption);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    resp.sendRedirect("AdminDashboard.html?success=Question added successfully");
                } else {
                    resp.sendRedirect("AdminDashboard.html?error=Failed to add question");
                }

            }  else {
                resp.sendRedirect("AdminDashboard.html?error=Invalid action");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            resp.sendRedirect("AdminDashboard.html?error=Database error");
        }
    }
}
