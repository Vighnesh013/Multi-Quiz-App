package com.aurionpro.multiQuiz;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/Question")
public class QuestionServlet extends HttpServlet {

    private static final int TOTAL_QUESTIONS = 5;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("UserLogin.html");
            return;
        }

        Integer currentQ = (Integer) session.getAttribute("currentQuestion");
        if (currentQ == null)
            currentQ = 1;

        int categoryId = (int) session.getAttribute("category_id");

        Boolean resetTimer = (Boolean) session.getAttribute("resetTimer");
        if (resetTimer == null) resetTimer = false;

        try (Connection con = new DatabaseConectivity().getConnection()) {
            String query = "SELECT * FROM questions WHERE category_id = ? ORDER BY question_id LIMIT ?,1";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, categoryId);
            stmt.setInt(2, currentQ - 1);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                resp.setContentType("text/html;charset=UTF-8");
                PrintWriter out = resp.getWriter();

                out.println("<!DOCTYPE html>");
                out.println("<html lang='en'>");
                out.println("<head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                out.println("<title>Question " + currentQ + "/" + TOTAL_QUESTIONS + "</title>");
                out.println("<link rel='stylesheet' type='text/css' href='Style.css' />");
                out.println("</head>");
                out.println("<body>");

                out.println("<main class='quiz-container'>");
                out.println("<header>");
                out.println("<h1 class='quiz-title'>Multi Quiz Application</h1>");
                out.println("<h2 class='question-number'>Question " + currentQ + " of " + TOTAL_QUESTIONS + "</h2>");
                out.println("</header>");

                out.println("<form method='post' action='Question' class='quiz-form'>");
                out.println("<p class='question-text'><strong>" + rs.getString("question_text") + "</strong></p>");

                out.println("<input type='hidden' name='question_id' value='" + rs.getInt("question_id") + "' />");

                out.println("<div class='options'>");
                out.println("<label class='option-label'><input type='radio' name='answer' value='A' /> "
                        + rs.getString("option_a") + "</label>");
                out.println("<label class='option-label'><input type='radio' name='answer' value='B' /> "
                        + rs.getString("option_b") + "</label>");
                out.println("<label class='option-label'><input type='radio' name='answer' value='C' /> "
                        + rs.getString("option_c") + "</label>");
                out.println("<label class='option-label'><input type='radio' name='answer' value='D' /> "
                        + rs.getString("option_d") + "</label>");
                out.println("</div>");

                out.println("<button type='submit' class='submit-btn'>Submit</button>");
                out.println("</form>");

                out.println("<div id='timers'>");
                out.println("<p>Question Timer: <span id='questionTimer'>60</span> seconds</p>");
                out.println("<p>Overall Timer: <span id='overallTimer'>300</span> seconds</p>");
                out.println("</div>");

                out.println("</main>");

                out.println("<script>");

                if (currentQ == 1 && resetTimer) {
                    session.removeAttribute("resetTimer");
                    out.println("(function() {");
                    out.println("  const catId = " + categoryId + ";");
                    out.println("  sessionStorage.removeItem('cat_' + catId + '_overall_timeLeft');");
                    for (int i = 1; i <= TOTAL_QUESTIONS; i++) {
                        out.println("  sessionStorage.removeItem('cat_' + catId + '_question_' + " + i + " + '_timeLeft');");
                    }
                    out.println("})();");
                }

                out.println("const categoryId = " + categoryId + ";");
                out.println("const questionNumber = " + currentQ + ";");

                out.println("const questionTimerElement = document.getElementById('questionTimer');");
                out.println("const overallTimerElement = document.getElementById('overallTimer');");

                out.println("const questionKey = 'cat_' + categoryId + '_question_' + questionNumber + '_timeLeft';");
                out.println("const overallKey = 'cat_' + categoryId + '_overall_timeLeft';");

                out.println("let questionTime = sessionStorage.getItem(questionKey) ? parseInt(sessionStorage.getItem(questionKey)) : 60;");
                out.println("let overallTime = sessionStorage.getItem(overallKey) ? parseInt(sessionStorage.getItem(overallKey)) : 300;");

                out.println("function updateTimers() {");
                out.println("  if (questionTime <= 0 || overallTime <= 0) {");
                out.println("    clearInterval(timerInterval);");
                out.println("    submitAnswerTimeout();");
                out.println("    return;");
                out.println("  }");
                out.println("  questionTimerElement.textContent = questionTime;");
                out.println("  overallTimerElement.textContent = overallTime;");
                out.println("  questionTime--;");
                out.println("  overallTime--;");
                out.println("  sessionStorage.setItem(questionKey, questionTime);");
                out.println("  sessionStorage.setItem(overallKey, overallTime);");
                out.println("}");

                out.println("function submitAnswerTimeout() {");
                out.println("  let form = document.querySelector('.quiz-form');");
                out.println("  let answerSelected = false;");
                out.println("  for(let elem of form.elements['answer']) {");
                out.println("    if(elem.checked) { answerSelected = true; break; }");
                out.println("  }");
                out.println("  if (!answerSelected) {");
                out.println("    let input = document.createElement('input');");
                out.println("    input.type = 'hidden';");
                out.println("    input.name = 'answer';");
                out.println("    input.value = '';");
                out.println("    form.appendChild(input);");
                out.println("  }");
                out.println("  form.submit();");
                out.println("}");

                out.println("const timerInterval = setInterval(updateTimers, 1000);");

                out.println("</script>");

                out.println("</body>");
                out.println("</html>");

                out.close();

            } else {
                resp.sendRedirect("ResultServlet");
            }

        } catch (Exception e) {
            resp.sendRedirect("CategoriesHtml.html?error=Failed to load questions");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("UserLogin.html");
            return;
        }

        String selectedAnswer = req.getParameter("answer");
        if (selectedAnswer == null) {
            selectedAnswer = ""; 
        }

        Integer currentQ = (Integer) session.getAttribute("currentQuestion");
        if (currentQ == null)
            currentQ = 1;

        @SuppressWarnings("unchecked")
        java.util.Map<Integer, String> answers = (java.util.Map<Integer, String>) session.getAttribute("answers");
        if (answers == null) {
            answers = new java.util.HashMap<>();
        }

        int questionId = Integer.parseInt(req.getParameter("question_id"));
        answers.put(questionId, selectedAnswer);
        session.setAttribute("answers", answers);

        currentQ++;
        session.setAttribute("currentQuestion", currentQ);

        if (currentQ > TOTAL_QUESTIONS) {
            resp.sendRedirect("ResultServlet");
        } else {
            resp.sendRedirect("Question");
        }
    }
}
