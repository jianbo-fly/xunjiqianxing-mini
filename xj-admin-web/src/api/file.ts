import { post } from '@/utils/request'

// 上传单个文件
export function uploadFile(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  return post('/admin/file/upload', formData)
}

// 批量上传文件
export function uploadFiles(files: File[]): Promise<string[]> {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return post('/admin/file/uploadBatch', formData)
}
