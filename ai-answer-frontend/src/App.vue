<template>
  <div class="app">
    <!-- 头部标题 -->
    <header class="app-header">
      <div class="header-content">
        <h1 class="app-title">面试题小助手</h1>
      </div>
    </header>

    <!-- 聊天区域 -->
    <main class="chat-container">
      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesContainer">

        <!-- 历史消息 -->
        <ChatMessage
            v-for="message in messages"
            :key="message.id"
            :message="message.content"
            :is-user="message.isUser"
            :timestamp="message.timestamp"
        />

        <!-- AI 正在回复的消息 -->
        <div v-if="isAiTyping" class="chat-message ai-message fade-in">
          <div class="message-avatar">
            <div class="avatar ai-avatar">AI</div>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="ai-typing-content">
                <div class="ai-response-text markdown-enhanced" v-html="currentAiResponseRendered"></div>
                <LoadingDots v-if="isStreaming" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域容器 -->
      <div class="input-area-container">
        <!-- 回到底部按钮 -->
        <button
            v-if="showScrollToBottomBtn"
            class="scroll-to-bottom-btn"
            @click="forceScrollToBottom"
            title="回到底部"
        >
          ↓
        </button>

        <!-- 输入框 -->
        <ChatInput
            :disabled="isAiTyping"
            @send-message="sendMessage"
            placeholder="请输入您的问题..."
        />
      </div>
    </main>

    <!-- 连接状态提示 -->
    <div v-if="connectionError" class="connection-error">
      <div class="error-content">
        <span class="error-icon">⚠️</span>
        <span>连接服务器失败，请检查后端服务是否启动</span>
      </div>
    </div>
  </div>
</template>

<script>
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import LoadingDots from './components/LoadingDots.vue'
import { chatWithSSE } from './api/chatApi.js'
import { generateMemoryId } from './utils/index.js'
import { marked } from 'marked'

export default {
  name: 'App',
  components: {
    ChatMessage,
    ChatInput,
    LoadingDots
  },
  data() {
    return {
      messages: [],
      memoryId: null,
      isAiTyping: false,
      isStreaming: false,
      currentAiResponse: '',
      currentEventSource: null,
      connectionError: false,
      showScrollToBottomBtn: false
    }
  },
  computed: {
    currentAiResponseRendered() {
      if (!this.currentAiResponse) return ''
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
      return marked(this.currentAiResponse)
    }
  },
  methods: {
    sendMessage(message) {
      // 添加用户消息
      this.addMessage(message, true)

      // 开始AI回复
      this.startAiResponse(message)
    },

    addMessage(content, isUser = false) {
      const message = {
        id: Date.now() + Math.random(),
        content,
        isUser,
        timestamp: new Date()
      }
      this.messages.push(message)
      this.scrollToBottom()
    },

    startAiResponse(userMessage) {
      this.isAiTyping = true
      this.isStreaming = true
      this.currentAiResponse = ''
      this.connectionError = false

      // 关闭之前的连接
      if (this.currentEventSource) {
        this.currentEventSource.close()
      }

      // 开始SSE连接
      this.currentEventSource = chatWithSSE(
          this.memoryId,
          userMessage,
          this.handleAiMessage,
          this.handleAiError,
          this.handleAiClose
      )
    },

    handleAiMessage(data) {
      this.currentAiResponse += data
      this.scrollToBottom()
    },

    handleAiError(error) {
      console.error('AI 回复出错:', error)
      this.connectionError = true
      this.finishAiResponse()

      // 5秒后自动隐藏错误提示
      setTimeout(() => {
        this.connectionError = false
      }, 5000)
    },

    handleAiClose() {
      this.finishAiResponse()
    },

    finishAiResponse() {
      this.isStreaming = false

      // 如果有内容，添加到消息列表
      if (this.currentAiResponse.trim()) {
        this.addMessage(this.currentAiResponse.trim(), false)
      }

      // 重置状态
      this.isAiTyping = false
      this.currentAiResponse = ''

      // 重置连接错误状态（确保正常结束时清除错误提示）
      this.connectionError = false

      // 关闭连接
      if (this.currentEventSource) {
        this.currentEventSource.close()
        this.currentEventSource = null
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          // 只有当用户当前滚动位置接近底部时才自动滚动到底部
          // 这里设置一个阈值（100px），如果用户向上滚动超过这个距离，就不再强制滚动
          const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 100
          if (isNearBottom) {
            container.scrollTop = container.scrollHeight
          }
        }
      })
    },

    // 处理滚动事件，更新回到底部按钮的显示状态
    handleScroll() {
      const container = this.$refs.messagesContainer
      if (container) {
        // 当用户向上滚动超过一定距离（100px）时显示回到底部按钮
        const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 100
        this.showScrollToBottomBtn = !isNearBottom && container.scrollHeight > container.clientHeight
      }
    },

    // 强制滚动到底部
    forceScrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    },

    initializeChat() {
      this.memoryId = generateMemoryId()
      console.log('聊天室ID:', this.memoryId)
    }
  },

  mounted() {
    this.initializeChat()
    // 监听滚动事件来显示/隐藏回到底部按钮
    const container = this.$refs.messagesContainer
    if (container) {
      container.addEventListener('scroll', this.handleScroll)
    }
  },

  beforeUnmount() {
    // 组件销毁前移除事件监听
    const container = this.$refs.messagesContainer
    if (container) {
      container.removeEventListener('scroll', this.handleScroll)
    }
    // 关闭之前的连接
    if (this.currentEventSource) {
      this.currentEventSource.close()
    }
  }
}
</script>

<style scoped>
.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--background-secondary);
  overflow: hidden;
}

.app-header {
  background-color: var(--background-primary);
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.header-content {
  width: 100%;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 100%;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}

/* AI 正在回复时的消息样式 */
.chat-message {
  display: flex;
  margin-bottom: 16px;
  padding: 0 16px;
  animation: fadeIn var(--transition-normal);
}

.ai-message {
  justify-content: flex-start;
  flex-direction: row;
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
  background-color: var(--background-primary);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.ai-typing-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-response-text {
  font-size: 14px;
  line-height: 1.5;
}

.connection-error {
  position: fixed;
  top: 16px;
  left: 16px;
  right: 16px;
  background-color: var(--error-color);
  color: white;
  padding: 10px 16px;
  border-radius: var(--border-radius-md);
  z-index: 1000;
  animation: slideDown var(--transition-normal);
}

.error-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-icon {
  font-size: 14px;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 输入区域容器 */
.input-area-container {
  position: relative;
}

/* 回到底部按钮样式 */
.scroll-to-bottom-btn {
  position: absolute;
  bottom: calc(100% + 10px);
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  border: none;
  font-size: 16px;
  cursor: pointer;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-fast);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .scroll-to-bottom-btn {
    bottom: calc(100% + 8px);
    right: 15px;
    width: 36px;
    height: 36px;
    font-size: 14px;
  }
}

.scroll-to-bottom-btn:hover {
  background-color: var(--primary-dark);
  transform: scale(1.1);
  box-shadow: var(--shadow-lg);
}

.scroll-to-bottom-btn:active {
  transform: scale(0.95);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 10px 14px;
  }

  .app-title {
    font-size: 14px;
  }

  .messages-container {
    padding: 12px 0;
  }

  .message-content {
    max-width: 85%;
  }

  .chat-message {
    padding: 0 12px;
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

  .connection-error {
    top: 10px;
    left: 10px;
    right: 10px;
    padding: 8px 12px;
  }

  .scroll-to-bottom-btn {
    bottom: 70px;
    right: 15px;
    width: 36px;
    height: 36px;
    font-size: 14px;
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .chat-message {
    padding: 0 8px;
  }

  .message-content {
    max-width: 88%;
  }
}
</style>