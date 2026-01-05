<template>
  <div class="section">
    <h2>고정 확장자</h2>
    <div class="checkbox-group">
      <label
        v-for="ext in extensions"
        :key="ext.extension"
        class="checkbox-item"
      >
        <input
          type="checkbox"
          :checked="ext.active"
          :disabled="loading"
          @change="handleToggle(ext.extension)"
        />
        <span>{{ ext.extension }}</span>
      </label>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FixedExtensions',
  props: {
    extensions: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle'],
  methods: {
    handleToggle(extension) {
      this.$emit('toggle', extension)
    }
  }
}
</script>

<style scoped>
.section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.section h2 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #ddd;
  transition: all 0.2s;
}

.checkbox-item:hover {
  border-color: #007bff;
}

.checkbox-item input {
  cursor: pointer;
  width: 16px;
  height: 16px;
}

.checkbox-item span {
  font-family: monospace;
  font-size: 14px;
}
</style>
