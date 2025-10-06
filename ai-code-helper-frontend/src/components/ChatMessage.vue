<template>
  <div class="chat-message" :class="{ 'user-message': isUser, 'ai-message': !isUser }">
    <div class="message-avatar">
      <div class="avatar" :class="{ 'user-avatar': isUser, 'ai-avatar': !isUser }">
        {{ isUser ? '我' : 'AI' }}
      </div>
    </div>
    <div class="message-content">
      <div class="message-bubble" :class="{ 'user-bubble': isUser, 'ai-bubble': !isUser }">
        <!-- 用户消息使用普通文本 -->
        <pre v-if="isUser" class="message-text">{{ message }}</pre>
        <!-- AI回复使用Markdown渲染 -->
        <div v-else class="message-markdown markdown-enhanced" v-html="renderedMessage"></div>
      </div>
      <div class="message-time">{{ formatTime(timestamp) }}</div>
    </div>
  </div>
</template>

<script>
import { formatTime } from '../utils/index.js'
import { marked } from 'marked'

export default {
  name: 'ChatMessage',
  props: {
    message: {
      type: String,
      required: true
    },
    isUser: {
      type: Boolean,
      default: false
    },
    timestamp: {
      type: Date,
      default: () => new Date()
    }
  },
  computed: {
    renderedMessage() {
      if (this.isUser) {
        return this.message
      }
      // 配置marked选项
      marked.setOptions({
        breaks: true, // 支持换行
        gfm: true, // 支持GitHub风格的Markdown
        sanitize: false, // 不过滤HTML（根据需要可以开启）
        highlight: function(code, lang) {
          // 可以在这里添加代码高亮功能
          return code
        }
      })
      return marked(this.message)
    }
  },
  methods: {
    formatTime
  }
}
</script>

<style scoped>
.chat-message {
  display: flex;
  margin-bottom: 16px;
  padding: 0 16px;
  animation: fadeIn var(--transition-normal);
}

.user-message {
  justify-content: flex-end;
  flex-direction: row;
}

.user-message .message-avatar {
  order: 2;
}

.user-message .message-content {
  order: 1;
}

.ai-message {
  justify-content: flex-start;
  flex-direction: row;
}

.ai-message .message-avatar {
  order: 1;
}

.ai-message .message-content {
  order: 2;
}

.message-avatar {
  display: flex;
  align-items: flex-start;
  margin: 0 8px;
  flex-shrink: 0;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.ai-avatar {
  background-color: var(--primary-color);
}

.user-avatar {
  background-color: var(--secondary-color);
}

.message-content {
  max-width: 80%;
  min-width: 100px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 8px;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
}

.user-bubble {
  background-color: var(--primary-color);
  color: white;
}

.ai-bubble {
  background-color: var(--background-primary);
  color: var(--text-primary);
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  color: var(--text-light);
  text-align: right;
  margin-top: 4px;
  margin-right: 8px;
  margin-left: 8px;
}

.user-message .message-time {
  text-align: left;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-message {
    padding: 0 12px;
  }

  .message-content {
    max-width: 85%;
  }

  .message-avatar {
    margin: 0 6px;
  }

  .avatar {
    width: 32px;
    height: 32px;
    font-size: 11px;
  }

  .message-bubble {
    padding: 10px 14px;
  }

  .message-text {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .chat-message {
    padding: 0 8px;
  }

  .message-content {
    max-width: 88%;
  }
}
</style>