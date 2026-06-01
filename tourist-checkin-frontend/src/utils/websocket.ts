type MessageHandler = (data: any) => void

const handlers: Map<string, Set<MessageHandler>> = new Map()
let ws: WebSocket | null = null
let reconnectTimer: number | null = null
let heartbeatTimer: number | null = null
let reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 10
let isLoggedIn = false

export function connectWebSocket(token: string) {
  if (!token) return

  isLoggedIn = true
  reconnectAttempts = 0
  const wsUrl = import.meta.env.VITE_WS_URL || `ws://localhost:8080/ws/notification`
  // 使用 Sec-WebSocket-Protocol 头部传递 token，避免暴露在 URL 参数中被服务器日志记录
  ws = new WebSocket(wsUrl, [token])

  ws.onopen = () => {
    console.log('WebSocket 已连接')
    reconnectAttempts = 0
    startHeartbeat()
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'pong') return
      const type = data.type || 'notification'
      handlers.get(type)?.forEach(fn => fn(data))
      handlers.get('*')?.forEach(fn => fn(data))
    } catch { /* ignore malformed messages */ }
  }

  ws.onclose = () => {
    stopHeartbeat()
    if (isLoggedIn) {
      scheduleReconnect(token)
    }
  }

  ws.onerror = () => {
    ws?.close()
  }
}

export function disconnectWebSocket() {
  isLoggedIn = false
  stopHeartbeat()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  handlers.clear()
  ws?.close()
  ws = null
}

export function onMessage(type: string, handler: MessageHandler) {
  if (!handlers.has(type)) handlers.set(type, new Set())
  handlers.get(type)!.add(handler)
}

export function offMessage(type: string, handler: MessageHandler) {
  handlers.get(type)?.delete(handler)
}

function startHeartbeat() {
  heartbeatTimer = window.setInterval(() => {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'ping' }))
    }
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function scheduleReconnect(token: string) {
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.log('WebSocket 重连次数超限，停止重连')
    return
  }

  // 指数退避：基础5秒，每次翻倍，最大60秒
  const delay = Math.min(5000 * Math.pow(2, reconnectAttempts), 60000)
  reconnectAttempts++

  console.log(`WebSocket 将在 ${delay / 1000}s 后重连 (第${reconnectAttempts}次)`)
  reconnectTimer = window.setTimeout(() => {
    if (isLoggedIn) {
      connectWebSocket(token)
    }
  }, delay)
}
