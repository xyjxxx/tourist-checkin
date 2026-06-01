import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

/**
 * 安全格式化时间 - 处理null/undefined情况
 * @param time 时间字符串或null
 * @param format 格式模板
 * @returns 格式化后的字符串
 */
export function formatDateSafe(time: string | null | undefined, format = 'YYYY-MM-DD HH:mm'): string {
  if (!time) {
    return '未知时间'
  }
  return dayjs(time).format(format)
}

/**
 * 安全显示相对时间
 * @param time 时间字符串或null
 * @returns 相对时间字符串
 */
export function formatRelativeTime(time: string | null | undefined): string {
  if (!time) {
    return '未知时间'
  }

  const checkTime = dayjs(time)
  const now = dayjs()
  const diffHours = now.diff(checkTime, 'hour')
  const diffDays = now.diff(checkTime, 'day')

  // 24小时内显示相对时间，超过24小时显示具体日期
  if (diffHours < 24) {
    return checkTime.fromNow()
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return checkTime.format('YYYY-MM-DD HH:mm')
  }
}

/**
 * 获取完整时间提示
 * @param time 时间字符串或null
 * @returns 完整时间字符串用于tooltip
 */
export function getFullDateTime(time: string | null | undefined): string {
  if (!time) {
    return '时间未记录'
  }
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}
