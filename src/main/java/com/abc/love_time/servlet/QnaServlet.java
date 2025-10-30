package com.abc.love_time.servlet;

import com.abc.love_time.dao.AnswerDAO;
import com.abc.love_time.dao.QuestionDAO;
import com.abc.love_time.dao.UserQuestionProgressDAO;
import com.abc.love_time.entity.Answer;
import com.abc.love_time.entity.Question;
import com.abc.love_time.util.JwtUtil;
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
 * 问答功能统一接口（适配前端要求）
 * GET  /api/qna/questions        - 获取问题列表
 * POST /api/qna/answer/submit    - 提交答案
 * GET  /api/qna/history          - 获取历史记录
 * GET  /api/qna/partner          - 获取对方答案
 * POST /api/qna/question/add     - 添加自定义问题
 * POST /api/qna/question/delete  - 删除自定义问题
 */
@WebServlet(name = "qnaServlet", value = "/api/qna/*")
public class QnaServlet extends HttpServlet {
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
            System.out.println("[QnaServlet] GET请求路径: " + pathInfo);

            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/questions")) {
                // 获取问题列表
                handleGetQuestions(request, response, out, userId);
            } else if (pathInfo.equals("/history")) {
                // 获取历史记录
                handleGetHistory(request, response, out, userId);
            } else if (pathInfo.equals("/partner")) {
                // 获取对方答案
                handleGetPartnerAnswer(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[QnaServlet] GET请求异常: " + e.getMessage());
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
            System.out.println("[QnaServlet] POST请求路径: " + pathInfo);

            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (pathInfo != null && pathInfo.equals("/answer/submit")) {
                // 提交答案
                handleSubmitAnswer(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/question/add")) {
                // 添加自定义问题
                handleAddQuestion(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/question/delete")) {
                // 删除自定义问题
                handleDeleteQuestion(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[QnaServlet] POST请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 从请求头中获取用户ID（通过JWT token）
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 这里简化处理，实际项目中应该从JWT中解析用户ID
                // 当前直接从URL参数获取（临时方案）
                String userIdParam = request.getParameter("userId");
                if (userIdParam != null && !userIdParam.isEmpty()) {
                    return Long.parseLong(userIdParam);
                }
            } catch (Exception e) {
                System.err.println("[QnaServlet] 解析token失败: " + e.getMessage());
            }
        }
        
        // 如果没有token，尝试从参数获取（开发阶段）
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.isEmpty()) {
            return Long.parseLong(userIdParam);
        }
        
        return null;
    }

    /**
     * 获取问题列表
     */
    private void handleGetQuestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        System.out.println("[QnaServlet] 获取用户 " + userId + " 的问题列表");

        // 查询预设问题和自定义问题
        List<Question> presetQuestions = questionDAO.findByCategory("preset");
        List<Question> customQuestions = questionDAO.findByUserId(userId);

        // 构建前端期望的格式
        List<Map<String, Object>> defaultQuestions = new ArrayList<>();
        for (Question q : presetQuestions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("text", q.getQuestionText());
            item.put("isDefault", true);
            defaultQuestions.add(item);
        }

        List<Map<String, Object>> customQuestionList = new ArrayList<>();
        for (Question q : customQuestions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("text", q.getQuestionText());
            item.put("isDefault", false);
            item.put("userId", q.getCreatedBy().toString());
            customQuestionList.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("defaultQuestions", defaultQuestions);
        data.put("customQuestions", customQuestionList);

        System.out.println("[QnaServlet] 返回 " + defaultQuestions.size() + " 个预设问题，" + customQuestionList.size() + " 个自定义问题");
        sendSuccess(response, out, data);
    }

    /**
     * 提交答案
     */
    private void handleSubmitAnswer(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) throws IOException {
        // 读取请求体
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        System.out.println("[QnaServlet] 提交答案请求: " + requestBody.toString());

        JsonObject jsonRequest = gson.fromJson(requestBody.toString(), JsonObject.class);
        Long questionId = jsonRequest.get("questionId").getAsLong();
        String answer = jsonRequest.get("answer").getAsString();

        // 验证参数
        if (answer == null || answer.trim().isEmpty()) {
            sendError(response, out, "答案内容不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

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
            existingAnswer.setAnswerText(answer);
            boolean updateSuccess = answerDAO.update(existingAnswer);
            
            if (!updateSuccess) {
                sendError(response, out, "更新答案失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            
            answerId = existingAnswer.getId();
            System.out.println("[QnaServlet] 答案已更新，ID: " + answerId);
        } else {
            // 插入新答案
            Answer newAnswer = new Answer(questionId, userId, answer);
            answerId = answerDAO.insert(newAnswer);
            
            if (answerId <= 0) {
                sendError(response, out, "保存答案失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            
            System.out.println("[QnaServlet] 新答案已保存，ID: " + answerId);
        }

        // TODO: 查询对方的答案（需要情侣关系表支持）
        // 当前返回模拟数据
        Map<String, Object> data = new HashMap<>();
        data.put("answerId", answerId);
        data.put("partnerAnswer", null);  // 暂时返回null
        data.put("hasPartnerAnswered", false);  // 暂时返回false

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "提交成功");
        result.put("data", data);

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(result));
    }

    /**
     * 获取历史记录（支持分页）
     */
    private void handleGetHistory(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        // 获取分页参数
        String pageParam = request.getParameter("page");
        String pageSizeParam = request.getParameter("pageSize");
        
        int page = (pageParam != null && !pageParam.isEmpty()) ? Integer.parseInt(pageParam) : 1;
        int pageSize = (pageSizeParam != null && !pageSizeParam.isEmpty()) ? Integer.parseInt(pageSizeParam) : 20;

        System.out.println("[QnaServlet] 查询用户 " + userId + " 的历史答案，页码: " + page + ", 每页: " + pageSize);

        // 查询用户所有答案
        List<Answer> allAnswers = answerDAO.findByUserId(userId);
        int total = allAnswers.size();

        // 分页处理
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);
        
        List<Answer> pagedAnswers = new ArrayList<>();
        if (startIndex < total) {
            pagedAnswers = allAnswers.subList(startIndex, endIndex);
        }

        // 构建响应，包含问题信息
        List<Map<String, Object>> historyList = new ArrayList<>();
        for (Answer answer : pagedAnswers) {
            Question question = questionDAO.findById(answer.getQuestionId());
            if (question != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", answer.getId());
                item.put("questionId", question.getId());
                item.put("question", question.getQuestionText());
                item.put("myAnswer", answer.getAnswerText());
                item.put("partnerAnswer", null);  // TODO: 需要查询对方答案
                item.put("createdAt", answer.getAnsweredAt().toString());
                item.put("updatedAt", answer.getUpdatedAt().toString());
                historyList.add(item);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("list", historyList);

        System.out.println("[QnaServlet] 返回 " + historyList.size() + " 条历史答案，总数: " + total);
        sendSuccess(response, out, data);
    }

    /**
     * 获取对方答案
     */
    private void handleGetPartnerAnswer(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        String questionIdParam = request.getParameter("questionId");
        
        if (questionIdParam == null || questionIdParam.trim().isEmpty()) {
            sendError(response, out, "questionId参数不能为空", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long questionId = Long.parseLong(questionIdParam);
        System.out.println("[QnaServlet] 查询问题 " + questionId + " 的对方答案");

        // TODO: 需要实现情侣关系表来获取对方ID
        // 当前返回模拟数据
        Map<String, Object> data = new HashMap<>();
        data.put("hasAnswered", false);
        data.put("answer", null);
        data.put("answeredAt", null);

        System.out.println("[QnaServlet] 暂未实现情侣关系，返回空数据");
        sendSuccess(response, out, data);
    }

    /**
     * 添加自定义问题
     */
    private void handleAddQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) throws IOException {
        // 读取请求体
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        System.out.println("[QnaServlet] 添加自定义问题请求: " + requestBody.toString());

        JsonObject jsonRequest = gson.fromJson(requestBody.toString(), JsonObject.class);
        String questionText = jsonRequest.get("text").getAsString();

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
            Map<String, Object> data = new HashMap<>();
            data.put("id", questionId);
            data.put("text", questionText);
            data.put("isDefault", false);
            data.put("userId", userId.toString());
            data.put("createdAt", new java.util.Date().toString());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "添加成功");
            result.put("data", data);

            System.out.println("[QnaServlet] 自定义问题添加成功，ID: " + questionId);
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
        } else {
            sendError(response, out, "添加问题失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除自定义问题
     */
    private void handleDeleteQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) throws IOException {
        // 读取请求体
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        System.out.println("[QnaServlet] 删除问题请求: " + requestBody.toString());

        JsonObject jsonRequest = gson.fromJson(requestBody.toString(), JsonObject.class);
        Long questionId = jsonRequest.get("questionId").getAsLong();

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

        // 验证是否是本人创建的问题
        if (!userId.equals(question.getCreatedBy())) {
            sendError(response, out, "只能删除自己创建的问题", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        boolean success = questionDAO.deleteById(questionId);

        if (success) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");

            System.out.println("[QnaServlet] 问题删除成功，ID: " + questionId);
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
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
