<template>
  <div class="section">
    <h2>커스텀 확장자</h2>

    <div class="input-area">
      <input
        v-model="newExtension"
        type="text"
        placeholder="확장자 입력 (예: sh, py)"
        :disabled="loading"
        @keyup.enter="handleAdd"
      />
      <button
        :disabled="loading || !newExtension.trim()"
        @click="handleAdd"
      >
        추가
      </button>
      <span class="count">{{ customCount }}/{{ maxCount }}</span>
    </div>

    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="tags-area">
      <span
        v-for="ext in extensions"
        :key="ext.extension"
        class="tag"
      >
        {{ ext.extension }}
        <button
          class="delete-btn"
          :disabled="loading"
          @click="handleDelete(ext.extension)"
        >
          ✕
        </button>
      </span>
      <p v-if="extensions.length === 0" class="empty-message">
        등록된 커스텀 확장자가 없습니다.
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomExtensions',
  props: {
    extensions: {
      type: Array,
      required: true
    },
    customCount: {
      type: Number,
      required: true
    },
    maxCount: {
      type: Number,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    },
    error: {
      type: String,
      default: ''
    }
  },
  emits: ['add', 'delete', 'clear-error'],
  data() {
    return {
      newExtension: ''
    }
  },
  watch: {
    newExtension() {
      this.$emit('clear-error')
    }
  },
  methods: {
    handleAdd() {
      if (this.newExtension.trim()) {
        this.$emit('add', this.newExtension.trim())
        this.newExtension = ''
      }
    },
    handleDelete(extension) {
      this.$emit('delete', extension)
    }
  }
}
</script>

<style scoped>
.section {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.section h2 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
}

.input-area {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.input-area input {
  flex: 1;
  max-width: 300px;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.input-area input:focus {
  outline: none;
  border-color: #007bff;
}

.input-area button {
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.input-area button:hover:not(:disabled) {
  background: #0056b3;
}

.input-area button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.count {
  font-size: 14px;
  color: #666;
}

.error-message {
  color: #dc3545;
  font-size: 14px;
  margin: 0 0 15px 0;
}

.tags-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  min-height: 40px;
  padding: 15px;
  background: white;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: #e9ecef;
  border-radius: 4px;
  font-family: monospace;
  font-size: 14px;
}

.delete-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 12px;
  padding: 2px 4px;
  border-radius: 2px;
  transition: all 0.2s;
}

.delete-btn:hover:not(:disabled) {
  background: #dc3545;
  color: white;
}

.delete-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.empty-message {
  color: #999;
  font-size: 14px;
  margin: 0;
}
</style>
