/**
 * 客户端图片压缩工具
 * 在上传前压缩图片，减少上传时间和存储成本
 */

interface CompressOptions {
  maxWidth?: number
  maxHeight?: number
  quality?: number
  maxSizeKB?: number
}

const DEFAULT_OPTIONS: CompressOptions = {
  maxWidth: 1920,
  maxHeight: 1080,
  quality: 0.8,
  maxSizeKB: 500,
}

export function compressImage(file: File, options: CompressOptions = {}): Promise<File> {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  // 如果文件已经很小，直接返回
  if (file.size / 1024 <= (opts.maxSizeKB || 500)) {
    return Promise.resolve(file)
  }

  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)

    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target?.result as string

      img.onload = () => {
        const canvas = document.createElement('canvas')
        let { width, height } = img

        // 按比例缩小
        if (opts.maxWidth && width > opts.maxWidth) {
          height = (height * opts.maxWidth) / width
          width = opts.maxWidth
        }
        if (opts.maxHeight && height > opts.maxHeight) {
          width = (width * opts.maxHeight) / height
          height = opts.maxHeight
        }

        canvas.width = width
        canvas.height = height

        const ctx = canvas.getContext('2d')
        if (!ctx) {
          reject(new Error('无法获取 Canvas 上下文'))
          return
        }

        ctx.drawImage(img, 0, 0, width, height)

        canvas.toBlob(
          (blob) => {
            if (!blob) {
              reject(new Error('图片压缩失败'))
              return
            }
            const compressedFile = new File([blob], file.name, {
              type: 'image/jpeg',
              lastModified: Date.now(),
            })
            resolve(compressedFile)
          },
          'image/jpeg',
          opts.quality
        )
      }

      img.onerror = () => reject(new Error('图片加载失败'))
    }

    reader.onerror = () => reject(new Error('文件读取失败'))
  })
}
