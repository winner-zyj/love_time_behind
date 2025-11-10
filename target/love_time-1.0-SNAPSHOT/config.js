// API 配置文件
const config = {
  // 基础URL
  BASE_URL: 'http://localhost:8080/lovetime',
  
  // API 路径配置
  API: {
    // 微信登录
    LOGIN: {
      WECHAT: '/api/login/wechat'
    },
    
    // 情侣关系管理
    COUPLE: {
      INVITE: {
        CREATE: '/api/couple/invite/create',      // 生成邀请码
        VALIDATE: '/api/couple/invite/validate'   // 验证邀请码
      },
      BIND: {
        ACCEPT: '/api/couple/bind/accept'         // 接受邀请（绑定）
      },
      STATUS: '/api/couple/status',               // 查询绑定状态
      UNBIND: '/api/couple/unbind'                // 解绑关系
    },
    
    // 甜蜜问答
    QNA: {
      LIST: '/api/qna/questions',                 // 获取问题列表
      CURRENT: '/api/qna/current',                // 获取当前问题
      SUBMIT: '/api/qna/submit',                  // 提交答案
      HISTORY: '/api/qna/history',                // 获取历史答案
      ADD_QUESTION: '/api/qna/question/add',      // 添加自定义问题
      DELETE_QUESTION: '/api/qna/question/delete' // 删除自定义问题
    },
    
    // 一百事挑战
    CHALLENGE: {
      TASKS: '/api/challenge/tasks',              // 获取任务列表
      PROGRESS: '/api/challenge/progress',        // 获取用户进度
      ADD_TASK: '/api/challenge/task/add',        // 添加自定义任务
      DELETE_TASK: '/api/challenge/task/delete',  // 删除自定义任务
      COMPLETE: '/api/challenge/complete',        // 标记任务完成/取消完成
      FAVORITE: '/api/challenge/favorite'         // 收藏/取消收藏任务
    },
    
    // 心形墙
    HEART_WALL: {
      PROJECTS: '/api/heart-wall/projects',       // 项目相关
      PHOTOS: '/api/heart-wall/photos',           // 照片相关
      NEXT_POSITION: '/api/heart-wall/next-position', // 获取下一个可用位置
      CLEAR_PHOTOS: '/api/heart-wall/clear-photos'    // 清空项目照片
    }
  }
};

export default config;