package com.abc.love_time.servlet;

import com.abc.love_time.dao.AnswerDAO;
import com.abc.love_time.dao.QuestionDAO;
import com.abc.love_time.dao.UserQuestionProgressDAO;
import com.abc.love_time.dto.QuestionListResponse;
import com.abc.love_time.dto.QuestionResponse;
import com.abc.love_time.entity.Answer;
import com.abc.love_time.entity.Question;
import com.abc.love_time.entity.UserQuestionProgress;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
 * 问题管理接口
 * GET  /api/questions/list        - 获取问题列表
 * GET  /api/questions/{id}        - 获取单个问题详情
 * GET  /api/questions/next        - 获取下一题
 * POST /api/questions/custom      - 添加自定义问题
 * DELETE /api/questions/{id}      - 删除自定义问题
 */
@WebServlet(name = "questionServlet", value = "/api/questions/*")
public class QuestionServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();
    private final UserQuestionProgressDAO progressDAO = new UserQuestionProgressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[QuestionServlet] GET请求路径: " + pathInfo);

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取问题列表
                handleGetQuestionList(request, response, out);
            } else if (pathInfo.equals("/next")) {
                // 获取下一题
                handleGetNextQuestion(request, response, out);
            } else if (pathInfo.matches("/\\d+")) {
                // 获取单个问题详情
                handleGetQuestionById(request, response, out, pathInfo);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[QuestionServlet] GET请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[QuestionServlet] POST请求路径: " + pathInfo);

            if (pathInfo != null && pathInfo.equals("/custom")) {
                // 添加自定义问题
                handleAddCustomQuestion(request, response, out);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[QuestionServlet] POST请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[QuestionServlet] DELETE请求路径: " + pathInfo);

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // 删除问题
                handleDeleteQuestion(request, response, out, pathInfo);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[QuestionServlet] DELETE请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 处理获取问题列表请求
     */
    private void handleGetQuestionList(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String userIdParam = request.getParameter("userId");
        
        if (userIdParam == null || userIdParam.trim().isEmpty()) {
            sendError(response, out, "userId参数不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long userId = Long.parseLong(userIdParam);
        System.out.println("[QuestionServlet] 获取用户 " + userId + " 的问题列表");

        // 查询预设问题和自定义问题
        List<Question> presetQuestions = questionDAO.findByCategory("preset");
        List<Question> customQuestions = questionDAO.findByUserId(userId);

        // 查询用户已回答的问题
        List<Answer> userAnswers = answerDAO.findByUserId(userId);
        Map<Long, String> answerMap = new HashMap<>();
        for (Answer answer : userAnswers) {
            answerMap.put(answer.getQuestionId(), answer.getAnswerText());
        }

        // 构建预设问题响应
        List<QuestionResponse> presetResponses = new ArrayList<>();
        for (Question q : presetQuestions) {
            QuestionResponse qr = new QuestionResponse(q.getId(), q.getQuestionText(), q.getCategory(), q.getOrderIndex());
            if (answerMap.containsKey(q.getId())) {
                qr.setHasAnswered(true);
                qr.setUserAnswer(answerMap.get(q.getId()));
            }
            presetResponses.add(qr);
        }

        // 构建自定义问题响应
        List<QuestionResponse> customResponses = new ArrayList<>();
        for (Question q : customQuestions) {
            QuestionResponse qr = new QuestionResponse(q.getId(), q.getQuestionText(), q.getCategory(), q.getOrderIndex());
            if (answerMap.containsKey(q.getId())) {
                qr.setHasAnswered(true);
                qr.setUserAnswer(answerMap.get(q.getId()));
            }
            customResponses.add(qr);
        }

        // 查询用户进度
        UserQuestionProgress progress = progressDAO.findByUserId(userId);
        int completedCount = answerDAO.countByUserId(userId);
        int totalCount = presetQuestions.size() + customQuestions.size();

        // 构建响应
        QuestionListResponse listResponse = new QuestionListResponse();
        listResponse.setPresetQuestions(presetResponses);
        listResponse.setCustomQuestions(customResponses);
        listResponse.setTotalCount(totalCount);
        listResponse.setCompletedCount(completedCount);
        listResponse.setCurrentQuestionId(progress != null ? progress.getCurrentQuestionId() : null);

        System.out.println("[QuestionServlet] 返回 " + presetResponses.size() + " 个预设问题，" + customResponses.size() + " 个自定义问题");
        sendSuccess(response, out, listResponse);
    }

    /**
     * 处理获取单个问题详情
     */
    private void handleGetQuestionById(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String pathInfo) {
        Long questionId = Long.parseLong(pathInfo.substring(1));
        System.out.println("[QuestionServlet] 获取问题详情，ID: " + questionId);

        Question question = questionDAO.findById(questionId);
        if (question == null) {
            sendError(response, out, "问题不存在", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        QuestionResponse qr = new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getCategory(),
                question.getOrderIndex()
        );

        System.out.println("[QuestionServlet] 返回问题详情: " + question.getQuestionText());
        sendSuccess(response, out, qr);
    }

    /**
     * 处理获取下一题请求
     */
    private void handleGetNextQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String userIdParam = request.getParameter("userId");
        String currentQuestionIdParam = request.getParameter("currentQuestionId");

        if (userIdParam == null || userIdParam.trim().isEmpty()) {
            sendError(response, out, "userId参数不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long userId = Long.parseLong(userIdParam);
        Long currentQuestionId = (currentQuestionIdParam != null && !currentQuestionIdParam.isEmpty()) 
                ? Long.parseLong(currentQuestionIdParam) : null;

        System.out.println("[QuestionServlet] 获取下一题，用户: " + userId + ", 当前问题: " + currentQuestionId);

        // 获取所有启用的问题
        List<Question> allQuestions = questionDAO.findAllActive();
        
        // 获取用户已回答的问题
        List<Answer> userAnswers = answerDAO.findByUserId(userId);
        Map<Long, Boolean> answeredMap = new HashMap<>();
        for (Answer answer : userAnswers) {
            answeredMap.put(answer.getQuestionId(), true);
        }

        // 找到下一个未回答的问题
        Question nextQuestion = null;
        boolean foundCurrent = (currentQuestionId == null);
        
        for (Question q : allQuestions) {
            if (foundCurrent && !answeredMap.containsKey(q.getId())) {
                nextQuestion = q;
                break;
            }
            if (currentQuestionId != null && q.getId().equals(currentQuestionId)) {
                foundCurrent = true;
            }
        }

        if (nextQuestion == null) {
            // 没有下一题了
            Map<String, Object> result = new HashMap<>();
            result.put("hasNext", false);
            result.put("message", "已完成所有问题");
            sendSuccess(response, out, result);
        } else {
            // 返回下一题
            QuestionResponse qr = new QuestionResponse(
                    nextQuestion.getId(),
                    nextQuestion.getQuestionText(),
                    nextQuestion.getCategory(),
                    nextQuestion.getOrderIndex()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasNext", true);
            result.put("question", qr);
            
            System.out.println("[QuestionServlet] 返回下一题: " + nextQuestion.getQuestionText());
            sendSuccess(response, out, result);
        }
    }

    /**
     * 处理添加自定义问题
     */
    private void handleAddCustomQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // 读取请求体
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        System.out.println("[QuestionServlet] 添加自定义问题请求: " + requestBody.toString());

        JsonObject jsonRequest = gson.fromJson(requestBody.toString(), JsonObject.class);
        Long userId = jsonRequest.get("userId").getAsLong();
        String questionText = jsonRequest.get("questionText").getAsString();

        if (questionText == null || questionText.trim().isEmpty()) {
            sendError(response, out, "问题内容不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 创建自定义问题
        Question question = new Question(questionText, "custom");
        question.setCreatedBy(userId);
        question.setIsActive(true);
        question.setOrderIndex(0);

        long questionId = questionDAO.insert(question);

        if (questionId > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("questionId", questionId);
            result.put("message", "自定义问题添加成功");
            
            System.out.println("[QuestionServlet] 自定义问题添加成功，ID: " + questionId);
            sendSuccess(response, out, result);
        } else {
            sendError(response, out, "添加问题失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理删除问题
     */
    private void handleDeleteQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String pathInfo) {
        Long questionId = Long.parseLong(pathInfo.substring(1));
        System.out.println("[QuestionServlet] 删除问题，ID: " + questionId);

        // 验证问题是否存在且为自定义问题
        Question question = questionDAO.findById(questionId);
        if (question == null) {
            sendError(response, out, "问题不存在", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!"custom".equals(question.getCategory())) {
            sendError(response, out, "只能删除自定义问题", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        boolean success = questionDAO.deleteById(questionId);

        if (success) {
            Map<String, String> result = new HashMap<>();
            result.put("message", "问题删除成功");
            
            System.out.println("[QuestionServlet] 问题删除成功，ID: " + questionId);
            sendSuccess(response, out, result);
        } else {
            sendError(response, out, "删除问题失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
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
