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

        <!-- 复制按钮（仅AI回复显示） -->
        <button v-if="!isUser" class="copy-btn" @click="copyMessage" :class="{ 'copied': copySuccess }" :title="copySuccess ? '已复制！' : '复制答案'">
          <svg class="copy-icon" viewBox="0 0 24 24" fill="none">
            <path v-if="!copySuccess" d="M8 16H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2m-6 12h8a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2h-8a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2z"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path v-else d="M20 6L9 17l-5-5"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <!-- 复制反馈提示 -->
      <div v-if="copySuccess" class="copy-feedback">已复制到剪贴板！</div>
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
  data() {
    return {
      copySuccess: false
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
    formatTime,
    // 复制方法
    async copyMessage() {
      try {
        // 优先使用现代 Clipboard API
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(this.message);
          this.showCopySuccess();
        } else {
          // 降级方案：使用传统的execCommand
          this.fallbackCopyText();
        }
      } catch (err) {
        console.error('复制失败:', err);
        // 最后的降级方案
        this.fallbackCopyText();
      }
    },

    // 传统的复制方法
    fallbackCopyText() {
      try {
        const textArea = document.createElement('textarea');
        textArea.value = this.message;

        // 避免滚动到元素
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';

        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        const successful = document.execCommand('copy');
        document.body.removeChild(textArea);

        if (successful) {
          this.showCopySuccess();
        } else {
          throw new Error('execCommand 复制失败');
        }
      } catch (err) {
        console.error('备用复制方法失败:', err);
        // 提示用户手动复制
        this.promptManualCopy();
      }
    },

    // 提示用户手动复制
    promptManualCopy() {
      alert('复制失败，请手动选择内容并按Ctrl+C复制');
    },

    // 显示复制成功提示
    showCopySuccess() {
      this.copySuccess = true;
      // 重置复制状态
      setTimeout(() => {
        this.copySuccess = false;
      }, 2000);
    }
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
  position: relative;
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
  border: 1px solid var(--border-color);
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

/* 新的复制按钮样式 */
.copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  background: #f6f8fa;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: 0;
  z-index: 10;
  padding: 0;
}

.ai-bubble:hover .copy-btn {
  opacity: 1;
}

.copy-btn:hover {
  background: #eaeef2;
  border-color: #afb8c1;
}

.copy-btn.copied {
  background: #dafbe1;
  border-color: #2da44e;
}

.copy-icon {
  width: 14px;
  height: 14px;
  color: #656d76;
  transition: color 0.2s ease;
}

.copy-btn:hover .copy-icon {
  color: #0969da;
}

.copy-btn.copied .copy-icon {
  color: #2da44e;
}

.copy-btn:active {
  transform: scale(0.95);
}

/* 复制成功反馈样式 */
.copy-feedback {
  position: absolute;
  top: -30px;
  right: 0;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  z-index: 20;
  animation: fadeInOut 2s ease-in-out;
  white-space: nowrap;
}

@keyframes fadeInOut {
  0% { opacity: 0; transform: translateY(10px); }
  20% { opacity: 1; transform: translateY(0); }
  80% { opacity: 1; transform: translateY(0); }
  100% { opacity: 0; transform: translateY(-10px); }
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

  .copy-btn {
    width: 26px;
    height: 26px;
    top: 6px;
    right: 6px;
  }

  .copy-icon {
    width: 13px;
    height: 13px;
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