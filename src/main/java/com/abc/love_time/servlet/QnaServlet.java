package com.abc.love_time.servlet;

import com.abc.love_time.dao.AnswerDAO;
import com.abc.love_time.dao.QuestionDAO;
import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dao.CoupleRelationshipDAO; // 添加情侣关系DAO
import com.abc.love_time.entity.Answer;
import com.abc.love_time.entity.Question;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 甜蜜问答接口
 * GET  /api/qna/questions - 获取问题列表
 * GET  /api/qna/current   - 获取当前问题
 * POST /api/qna/submit    - 提交答案（提交后返回情侣答案）
 * GET  /api/qna/history   - 获取历史答案
 * POST /api/qna/question/add - 添加自定义问题
 */
@WebServlet(name = "qnaServlet", value = "/api/qna/*")
public class QnaServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO(); // 添加情侣关系DAO

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[QnaServlet] GET请求路径: " + pathInfo);

            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 获取用户ID
            Long userId = getUserIdByCode(userCode);
            if (userId == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/questions")) {
                // 获取所有问题列表
                handleGetQuestions(request, response, out, userId);
            } else if (pathInfo.equals("/current")) {
                // 获取当前问题
                handleGetCurrentQuestion(request, response, out, userId);
            } else if (pathInfo.equals("/history")) {
                // 获取历史答案
                handleGetHistory(request, response, out, userId);
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

            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 获取用户ID
            Long userId = getUserIdByCode(userCode);
            if (userId == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo != null && (pathInfo.equals("/submit") || pathInfo.equals("/answer/submit"))) {
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
     * 获取问题列表
     */
    private void handleGetQuestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 获取所有问题（预设+自定义）
            List<Question> allQuestions = questionDAO.findAllQuestions();
            
            System.out.println("[QnaServlet] 获取问题列表，共 " + allQuestions.size() + " 个问题");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "获取成功");
            result.put("questions", allQuestions);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("[QnaServlet] 获取问题列表失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取问题列表失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取当前问题
     */
    private void handleGetCurrentQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 获取所有问题
            List<Question> allQuestions = questionDAO.findAllQuestions();
            
            if (allQuestions.isEmpty()) {
                sendError(response, out, "暂无问题", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 获取用户已回答的问题数
            int answeredCount = answerDAO.countByUserId(userId);
            
            // 获取当前应该回答的问题（简单轮询）
            int currentIndex = answeredCount % allQuestions.size();
            Question currentQuestion = allQuestions.get(currentIndex);
            
            System.out.println("[QnaServlet] 用户 " + userId + " 当前问题: " + currentQuestion.getText());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "获取成功");
            result.put("question", currentQuestion);
            result.put("answeredCount", answeredCount);
            result.put("totalCount", allQuestions.size());
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("[QnaServlet] 获取当前问题失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取当前问题失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 提交答案（提交后返回情侣答案）
     */
    private void handleSubmitAnswer(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 验证请求参数
            if (!jsonRequest.has("questionId") || jsonRequest.get("questionId").isJsonNull()) {
                sendError(response, out, "问题ID不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (!jsonRequest.has("answer") || jsonRequest.get("answer").isJsonNull()) {
                sendError(response, out, "答案不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long questionId = jsonRequest.get("questionId").getAsLong();
            String answerText = jsonRequest.get("answer").getAsString();
            
            if (answerText.trim().isEmpty()) {
                sendError(response, out, "答案不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // 检查问题是否存在
            Question question = questionDAO.findById(questionId);
            if (question == null) {
                sendError(response, out, "问题不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 检查是否已回答过该问题
            Answer existingAnswer = answerDAO.findByUserAndQuestion(userId, questionId);
            
            long answerId;
            if (existingAnswer != null) {
                // 更新已有答案
                existingAnswer.setAnswerText(answerText);
                answerDAO.update(existingAnswer);
                answerId = existingAnswer.getId();
                System.out.println("[QnaServlet] 更新答案成功，ID: " + answerId);
            } else {
                // 创建新答案
                Answer newAnswer = new Answer();
                newAnswer.setUserId(userId);
                newAnswer.setQuestionId(questionId);
                newAnswer.setAnswerText(answerText);
                answerId = answerDAO.insert(newAnswer);
                System.out.println("[QnaServlet] 提交新答案成功，ID: " + answerId);
            }
            
            if (answerId <= 0) {
                sendError(response, out, "答案保存失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            
            // 【关键功能】获取情侣的答案
            Answer partnerAnswer = answerDAO.findPartnerAnswerByQuestionId(questionId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", existingAnswer != null ? "答案更新成功" : "答案提交成功");
            result.put("answerId", answerId);
            
            // 返回情侣答案信息
            if (partnerAnswer != null) {
                result.put("partnerAnswer", partnerAnswer.getAnswerText());
                result.put("hasPartnerAnswer", true);
                result.put("partnerAnsweredAt", partnerAnswer.getAnsweredAt());
                System.out.println("[QnaServlet] 找到情侣答案");
            } else {
                result.put("partnerAnswer", null);
                result.put("hasPartnerAnswer", false);
                System.out.println("[QnaServlet] 情侣未回答该问题");
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("[QnaServlet] 提交答案失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "提交答案失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取历史答案
     */
    private void handleGetHistory(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 获取用户的所有答案
            List<Answer> answers = answerDAO.findByUserId(userId);
            
            System.out.println("[QnaServlet] 用户 " + userId + " 的历史答案数: " + answers.size());

            // 构建包含问题文本的历史记录
            List<Map<String, Object>> historyList = new ArrayList<>();
            for (Answer answer : answers) {
                Map<String, Object> historyItem = new HashMap<>();
                historyItem.put("id", answer.getId());
                historyItem.put("questionId", answer.getQuestionId());
                historyItem.put("answer", answer.getAnswerText());
                historyItem.put("answeredAt", answer.getAnsweredAt());
                historyItem.put("updatedAt", answer.getUpdatedAt());
                
                // 获取问题文本
                Question question = questionDAO.findById(answer.getQuestionId());
                if (question != null) {
                    historyItem.put("questionText", question.getQuestionText());
                    historyItem.put("questionCategory", question.getCategory());
                } else {
                    historyItem.put("questionText", "问题已删除");
                    historyItem.put("questionCategory", "unknown");
                }
                
                historyList.add(historyItem);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "获取成功");
            result.put("history", historyList);
            result.put("totalCount", historyList.size());
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("[QnaServlet] 获取历史答案失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取历史答案失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 添加自定义问题
     */
    private void handleAddQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 检查问题文本参数
            if (!jsonRequest.has("text") || jsonRequest.get("text").isJsonNull()) {
                sendError(response, out, "问题文本不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            String questionText = jsonRequest.get("text").getAsString();
            if (questionText.trim().isEmpty()) {
                sendError(response, out, "问题文本不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 创建自定义问题
            Question customQuestion = new Question();
            customQuestion.setText(questionText);
            customQuestion.setType("custom");
            customQuestion.setCreatorId(userId);
            
            long questionId = questionDAO.insert(customQuestion);
            
            if (questionId > 0) {
                customQuestion.setId(questionId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "问题添加成功");
                result.put("question", customQuestion);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
            } else {
                sendError(response, out, "问题添加失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[QnaServlet] 添加自定义问题失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "添加自定义问题失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除自定义问题
     */
    private void handleDeleteQuestion(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 检查问题ID参数
            if (!jsonRequest.has("questionId") || jsonRequest.get("questionId").isJsonNull()) {
                sendError(response, out, "问题ID不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long questionId = jsonRequest.get("questionId").getAsLong();
            
            // 检查问题是否存在
            Question question = questionDAO.findById(questionId);
            if (question == null) {
                sendError(response, out, "问题不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 检查是否是该用户创建的自定义问题
            if (!"custom".equals(question.getCategory()) || !userId.equals(question.getCreatedBy())) {
                sendError(response, out, "只能删除自己创建的自定义问题", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // 执行删除（逻辑删除）
            boolean deleted = questionDAO.deleteById(questionId);
            
            if (deleted) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "问题删除成功");
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[QnaServlet] 用户 " + userId + " 删除问题 " + questionId + " 成功");
            } else {
                sendError(response, out, "问题删除失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[QnaServlet] 删除自定义问题失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "删除自定义问题失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从请求头中获取用户code（通过JWT token）
     */
    private String getUserCodeFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("[QnaServlet] Authorization请求头: " + authHeader);
        
        if (authHeader == null) {
            System.err.println("[QnaServlet] 缺少Authorization请求头");
            return null;
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            System.err.println("[QnaServlet] Authorization格式错误，应为 'Bearer {token}'");
            return null;
        }
        
        String token = authHeader.substring(7);
        System.out.println("[QnaServlet] 解析到的token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        try {
            if (JwtUtil.validateToken(token)) {
                String userCode = JwtUtil.getOpenidFromToken(token);
                System.out.println("[QnaServlet] 从token解析用户code: " + userCode);
                return userCode;
            } else {
                System.err.println("[QnaServlet] Token验证失败");
            }
        } catch (Exception e) {
            System.err.println("[QnaServlet] 解析token失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据用户code获取用户ID
     */
    private Long getUserIdByCode(String userCode) {
        try {
            User user = userDAO.findByCode(userCode);
            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            System.err.println("[QnaServlet] 获取用户ID失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, PrintWriter out, String message, int statusCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        
        response.setStatus(statusCode);
        out.print(gson.toJson(error));
    }
}