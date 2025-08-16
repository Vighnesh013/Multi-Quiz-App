package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/ResponseServlet")
public class ResponseServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession(false);

		if (session == null || session.getAttribute("user_id") == null) {
			resp.sendRedirect("UserLogin.html");
			return;
		}

		@SuppressWarnings("unchecked")
		Map<Integer, String> answers = (Map<Integer, String>) session.getAttribute("answers");

		if (answers == null || answers.isEmpty()) {
			resp.sendRedirect("CategoriesHtml.html?error=No answers found");
			return;
		}

		try (Connection con = new DatabaseConectivity().getConnection()) {

			StringBuilder questionIds = new StringBuilder();
			for (Integer qId : answers.keySet()) {
				questionIds.append(qId).append(",");
			}
			questionIds.deleteCharAt(questionIds.length() - 1);

			String query = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE question_id IN ("
					+ questionIds + ")";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			Map<Integer, Question> questionMap = new HashMap<>();
			while (rs.next()) {
				Question q = new Question();
				q.id = rs.getInt("question_id");
				q.text = rs.getString("question_text");
				q.optionA = rs.getString("option_a");
				q.optionB = rs.getString("option_b");
				q.optionC = rs.getString("option_c");
				q.optionD = rs.getString("option_d");
				q.correctOption = rs.getString("correct_option");
				questionMap.put(q.id, q);
			}
			rs.close();
			stmt.close();

			resp.setContentType("text/html;charset=UTF-8");
			PrintWriter out = resp.getWriter();

			out.println("<!DOCTYPE html>");
			out.println("<html lang='en'>");
			out.println("<head>");
			out.println("<meta charset='UTF-8'>");
			out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
			out.println("<title>Your Detailed Responses</title>");
			out.println("<link rel='stylesheet' href='Style.css' />"); 
			out.println("</head>");
			out.println("<body>");
			out.println("<main class='response-container'>");
			out.println("<h2>Your Detailed Responses</h2>");
			out.println("<table class='response-table'>");
			out.println("<thead>");
			out.println("<tr><th>Question</th><th>Your Answer</th><th>Correct Answer</th></tr>");
			out.println("</thead>");
			out.println("<tbody>");

			for (Map.Entry<Integer, String> entry : answers.entrySet()) {
				int qId = entry.getKey();
				String userAns = entry.getValue();

				Question q = questionMap.get(qId);
				if (q == null)
					continue;

				String userAnswerText = getOptionText(q, userAns);
				String correctAnswerText = getOptionText(q, q.correctOption);

				String userAnswerDisplay = userAns.equalsIgnoreCase(q.correctOption)
						? userAnswerText
						: "<span class='wrong-answer'>" + userAnswerText + "</span>";

				out.println("<tr>");
				out.println("<td>" + q.text + "</td>");
				out.println("<td>" + userAnswerDisplay + "</td>");
				out.println("<td>" + correctAnswerText + "</td>");
				out.println("</tr>");
			}

			out.println("</tbody>");
			out.println("</table>");

			out.println("<section class='actions'>");
			out.println("<a href='CategoriesHtml.html' class='btn btn-secondary'>Take Another Test</a>");
			out.println("<a href='LogoutServlet' class='btn btn-logout'>Logout</a>");
			out.println("</section>");

			out.println("</main>");
			out.println("</body>");
			out.println("</html>");

			out.close();

			
			session.removeAttribute("answers");
			session.removeAttribute("currentQuestion");
			session.removeAttribute("category_id");

		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect("CategoriesHtml.html?error=Error loading detailed responses");
		}
	}

	private String getOptionText(Question q, String option) {
		switch (option.toUpperCase()) {
		case "A":
			return q.optionA;
		case "B":
			return q.optionB;
		case "C":
			return q.optionC;
		case "D":
			return q.optionD;
		default:
			return "Unknown";
		}
	}

	private static class Question {
		int id;
		String text;
		String optionA;
		String optionB;
		String optionC;
		String optionD;
		String correctOption;
	}
}
