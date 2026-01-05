<template>
  <div class="app">
    <header>
      <h1>파일 확장자 차단 관리</h1>
    </header>

    <main>
      <div v-if="initialLoading" class="loading">
        데이터를 불러오는 중...
      </div>

      <template v-else>
        <FixedExtensions
          :extensions="fixedExtensions"
          :loading="loading"
          @toggle="handleToggleFixed"
        />

        <CustomExtensions
          :extensions="customExtensions"
          :custom-count="customCount"
          :max-count="maxCustomCount"
          :loading="loading"
          :error="error"
          @add="handleAddCustom"
          @delete="handleDeleteCustom"
          @clear-error="error = ''"
        />
      </template>
    </main>
  </div>
</template>

<script>
import { extensionApi } from './api/extensionApi'
import FixedExtensions from './components/FixedExtensions.vue'
import CustomExtensions from './components/CustomExtensions.vue'

export default {
  name: 'App',
  components: {
    FixedExtensions,
    CustomExtensions
  },
  data() {
    return {
      fixedExtensions: [],
      customExtensions: [],
      customCount: 0,
      maxCustomCount: 200,
      loading: false,
      initialLoading: true,
      error: ''
    }
  },
  mounted() {
    this.fetchExtensions()
  },
  methods: {
    async fetchExtensions() {
      try {
        const response = await extensionApi.getAll()
        const data = response.data
        this.fixedExtensions = data.fixedExtensions
        this.customExtensions = data.customExtensions
        this.customCount = data.customCount
        this.maxCustomCount = data.maxCustomCount
      } catch (err) {
        this.error = '데이터를 불러오는데 실패했습니다.'
        console.error(err)
      } finally {
        this.initialLoading = false
      }
    },

    async handleToggleFixed(extension) {
      this.loading = true
      this.error = ''
      try {
        await extensionApi.toggleFixed(extension)
        await this.fetchExtensions()
      } catch (err) {
        this.error = err.response?.data?.message || '변경에 실패했습니다.'
      } finally {
        this.loading = false
      }
    },

    async handleAddCustom(extension) {
      this.loading = true
      this.error = ''
      try {
        await extensionApi.addCustom(extension)
        await this.fetchExtensions()
      } catch (err) {
        this.error = err.response?.data?.message || '추가에 실패했습니다.'
      } finally {
        this.loading = false
      }
    },

    async handleDeleteCustom(extension) {
      this.loading = true
      this.error = ''
      try {
        await extensionApi.deleteCustom(extension)
        await this.fetchExtensions()
      } catch (err) {
        this.error = err.response?.data?.message || '삭제에 실패했습니다.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f0f2f5;
  min-height: 100vh;
}

.app {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

header {
  margin-bottom: 30px;
}

header h1 {
  font-size: 24px;
  color: #333;
}

main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>
