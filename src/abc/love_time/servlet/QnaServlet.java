            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/questions")) {
                // 获取所有问题列表
                handleGetQuestions(request, response, out, userId);
            } else if (pathInfo.equals("/current")) {
                // 获取当前问题
                handleGetCurrentQuestion(request, response, out, userId);
            } else if (pathInfo.equals("/history")) {
                // 获取历史答案
                handleGetHistory(request, response, out, userId);
            } else if (pathInfo.equals("/partner")) {
                // 获取情侣答案
                handleGetPartnerAnswer(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }