package com.abc.love_time.servlet;

import com.abc.love_time.dao.AnswerDAO;
import com.abc.love_time.dao.QuestionDAO;
import com.abc.love_time.dao.UserQuestionProgressDAO;
import com.abc.love_time.dto.AnswerRequest;
import com.abc.love_time.entity.Answer;
import com.abc.love_time.entity.Question;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 答案提交接口
 * POST /api/answers/submit    - 提交答案
 * GET  /api/answers/history   - 查看历史答案
 */
@WebServlet(name = "answerServlet", value = "/api/answers/*")
public class AnswerServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final AnswerDAO answerDAO = new AnswerDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final UserQuestionProgressDAO progressDAO = new UserQuestionProgressDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[AnswerServlet] POST请求路径: " + pathInfo);

            if (pathInfo != null && pathInfo.equals("/submit")) {
                // 提交答案
                handleSubmitAnswer(request, response, out);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[AnswerServlet] POST请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[AnswerServlet] GET请求路径: " + pathInfo);

            if (pathInfo != null && pathInfo.equals("/history")) {
                // 查看历史答案
                handleGetHistory(request, response, out);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[AnswerServlet] GET请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 处理提交答案请求
     */
    private void handleSubmitAnswer(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // 读取请求体
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        System.out.println("[AnswerServlet] 提交答案请求: " + requestBody.toString());

        // 解析请求
        AnswerRequest answerRequest = gson.fromJson(requestBody.toString(), AnswerRequest.class);

        // 验证参数
        if (answerRequest.getUserId() == null) {
            sendError(response, out, "userId不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (answerRequest.getQuestionId() == null) {
            sendError(response, out, "questionId不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (answerRequest.getAnswerText() == null || answerRequest.getAnswerText().trim().isEmpty()) {
            sendError(response, out, "答案内容不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long userId = answerRequest.getUserId();
        Long questionId = answerRequest.getQuestionId();
        String answerText = answerRequest.getAnswerText();

        System.out.println("[AnswerServlet] 用户 " + userId + " 回答问题 " + questionId);

        // 验证问题是否存在
        Question question = questionDAO.findById(questionId);
        if (question == null) {
            sendError(response, out, "问题不存在", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 检查用户是否已回答过该问题
        Answer existingAnswer = answerDAO.findByUserAndQuestion(userId, questionId);

        long answerId;
        if (existingAnswer != null) {
            // 更新现有答案
            existingAnswer.setAnswerText(answerText);
            boolean updateSuccess = answerDAO.update(existingAnswer);
            
            if (!updateSuccess) {
                sendError(response, out, "更新答案失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            
            answerId = existingAnswer.getId();
            System.out.println("[AnswerServlet] 答案已更新，ID: " + answerId);
        } else {
            // 插入新答案
            Answer newAnswer = new Answer(questionId, userId, answerText);
            answerId = answerDAO.insert(newAnswer);
            
            if (answerId <= 0) {
                sendError(response, out, "保存答案失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            
            System.out.println("[AnswerServlet] 新答案已保存，ID: " + answerId);

            // 更新用户答题进度
            updateUserProgress(userId);
        }

        // 获取下一题信息
        List<Question> allQuestions = questionDAO.findAllActive();
        List<Answer> userAnswers = answerDAO.findByUserId(userId);
        
        Map<Long, Boolean> answeredMap = new HashMap<>();
        for (Answer ans : userAnswers) {
            answeredMap.put(ans.getQuestionId(), true);
        }

        // 找到下一个未回答的问题
        Question nextQuestion = null;
        for (Question q : allQuestions) {
            if (!answeredMap.containsKey(q.getId())) {
                nextQuestion = q;
                break;
            }
        }

        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("answerId", answerId);
        result.put("message", "答案提交成功");
        result.put("hasNext", nextQuestion != null);
        
        if (nextQuestion != null) {
            Map<String, Object> nextQuestionInfo = new HashMap<>();
            nextQuestionInfo.put("id", nextQuestion.getId());
            nextQuestionInfo.put("questionText", nextQuestion.getQuestionText());
            result.put("nextQuestion", nextQuestionInfo);
        }

        System.out.println("[AnswerServlet] 答案提交成功，hasNext: " + (nextQuestion != null));
        sendSuccess(response, out, result);
    }

    /**
     * 处理查看历史答案请求
     */
    private void handleGetHistory(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String userIdParam = request.getParameter("userId");
        
        if (userIdParam == null || userIdParam.trim().isEmpty()) {
            sendError(response, out, "userId参数不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long userId = Long.parseLong(userIdParam);
        System.out.println("[AnswerServlet] 查询用户 " + userId + " 的历史答案");

        // 查询用户所有答案
        List<Answer> answers = answerDAO.findByUserId(userId);

        // 构建响应，包含问题信息
        List<Map<String, Object>> historyList = new ArrayList<>();
        for (Answer answer : answers) {
            Question question = questionDAO.findById(answer.getQuestionId());
            if (question != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("answerId", answer.getId());
                item.put("questionId", question.getId());
                item.put("questionText", question.getQuestionText());
                item.put("answerText", answer.getAnswerText());
                item.put("answeredAt", answer.getAnsweredAt().toString());
                item.put("category", question.getCategory());
                historyList.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", historyList.size());
        result.put("history", historyList);

        System.out.println("[AnswerServlet] 返回 " + historyList.size() + " 条历史答案");
        sendSuccess(response, out, result);
    }

    /**
     * 更新用户答题进度
     */
    private void updateUserProgress(Long userId) {
        int completedCount = answerDAO.countByUserId(userId);
        int totalCount = questionDAO.countActive();
        
        // 获取下一个未回答的问题
        List<Question> allQuestions = questionDAO.findAllActive();
        List<Answer> userAnswers = answerDAO.findByUserId(userId);
        
        Map<Long, Boolean> answeredMap = new HashMap<>();
        for (Answer ans : userAnswers) {
            answeredMap.put(ans.getQuestionId(), true);
        }

        Long nextQuestionId = null;
        for (Question q : allQuestions) {
            if (!answeredMap.containsKey(q.getId())) {
                nextQuestionId = q.getId();
                break;
            }
        }

        // 更新进度
        progressDAO.initOrUpdate(userId, nextQuestionId, totalCount);
        
        System.out.println("[AnswerServlet] 用户进度已更新: " + completedCount + "/" + totalCount);
    }

    /**
     * 发送成功响应
     */
    private void sendSuccess(HttpServletResponse response, PrintWriter out, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        response.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(result));
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, PrintWriter out, String message, int statusCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        response.setStatus(statusCode);
        out.print(gson.toJson(result));
    }
}
