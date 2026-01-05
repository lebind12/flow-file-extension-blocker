import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

export const extensionApi = {
  getAll() {
    return api.get('/extensions')
  },

  toggleFixed(extension) {
    return api.patch(`/extensions/fixed/${extension}`)
  },

  addCustom(extension) {
    return api.post('/extensions/custom', { extension })
  },

  deleteCustom(extension) {
    return api.delete(`/extensions/custom/${extension}`)
  }
}
