<template>
  <div class="chat-input">
    <div class="input-container">
      <!-- 输入框和发送按钮 -->
      <textarea
        ref="inputRef"
        v-model="inputMessage"
        :placeholder="placeholder"
        :disabled="disabled"
        class="input-textarea"
        rows="1"
        @keydown="handleKeyDown"
        @input="adjustHeight"
      />
      <button
        :disabled="disabled || !inputMessage.trim()"
        @click="sendMessage"
        class="send-button"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M2 21l21-9L2 3v7l15 2-15 2v7z" fill="currentColor"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: '请输入您的问题...'
    }
  },
  data() {
    return {
      inputMessage: ''
    }
  },
  methods: {
    sendMessage() {
      if (this.inputMessage.trim() && !this.disabled) {
        this.$emit('send-message', this.inputMessage.trim())
        this.inputMessage = ''
        this.adjustHeight()
      }
    },
    handleKeyDown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        this.sendMessage()
      }
    },
    adjustHeight() {
      this.$nextTick(() => {
        const textarea = this.$refs.inputRef
        textarea.style.height = 'auto'
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
      })
    },
    focus() {
      this.$refs.inputRef.focus()
    }
  },
  mounted() {
    this.adjustHeight()
  }
}
</script>

<style scoped>
.chat-input {
  padding: 8px 16px;
  background-color: var(--background-primary);
  border-top: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.input-container {
  display: flex;
  align-items: stretch;
  gap: 8px;
  position: relative;
  flex-direction: column;
}

/* 工具栏样式 */
.input-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.toolbar-button {
  width: 28px;
  height: 28px;
  border: none;
  background-color: transparent;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  transition: all var(--transition-fast);
  font-size: 14px;
}

.toolbar-button:hover {
  background-color: var(--background-secondary);
  color: var(--text-primary);
}

.toolbar-separator {
  width: 1px;
  height: 16px;
  background-color: var(--border-color);
  margin: 0 4px;
}

.toolbar-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-right: 4px;
}

.toolbar-dropdown-icon {
  color: var(--text-secondary);
}

/* 输入框样式 */
.input-textarea {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  outline: none;
  transition: all var(--transition-fast);
  background-color: var(--background-primary);
  font-family: inherit;
  min-height: 36px;
  max-height: 120px;
  overflow-y: auto;
  width: 100%;
  color: var(--text-primary);
}

.input-textarea:focus {
  border-color: var(--primary-color);
  background-color: var(--background-primary);
}

.input-textarea:disabled {
  background-color: var(--background-tertiary);
  color: var(--text-light);
  cursor: not-allowed;
}

/* 发送按钮样式 */
.send-button {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 4px;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  align-self: flex-end;
  margin-top: 8px;
}

.send-button:hover:not(:disabled) {
  background-color: var(--primary-dark);
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button:disabled {
  background-color: var(--text-light);
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-input {
    padding: 8px 12px;
  }

  .input-container {
    gap: 6px;
  }

  .input-toolbar {
    gap: 8px;
  }

  .input-textarea {
    padding: 8px 10px;
    font-size: 13px;
  }

  .send-button {
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 480px) {
  .chat-input {
    padding: 6px 8px;
  }

  .input-toolbar {
    gap: 6px;
  }

  .toolbar-button {
    width: 24px;
    height: 24px;
  }

  .input-textarea {
    padding: 6px 8px;
  }

  .send-button {
    width: 28px;
    height: 28px;
  }
}
</style>