export type UserRole = 'USER' | 'ADMIN' | 'SUPER_ADMIN'

export interface UserInfo {
  id: number
  account: string
  username: string
  avatar: string
  backgroundImage: string
  email: string
  role: UserRole
  createdAt: string
}

export interface LoginForm {
  account: string
  password: string
}

export interface RegisterForm {
  account: string
  username?: string
  password: string
  email: string
}

export interface CheckIn {
  id: number
  userId: number
  username: string
  avatar: string
  locationId: number | null
  longitude: number
  latitude: number
  locationName: string
  content: string
  images: string[]
  likeCount: number
  hasLiked: boolean
  checkInTime: string
}

export interface CheckInForm {
  locationId?: number
  longitude: number
  latitude: number
  locationName: string
  content?: string
  images?: string[]
}

export interface Location {
  id: number
  name: string
  address: string
  longitude: number
  latitude: number
  category: string
  city: string
  description: string
  coverImage: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface AdminUser {
  id: number
  username: string
  avatar: string
  email: string
  role: UserRole
  createdAt: string
}

export interface AdminUserList {
  list: AdminUser[]
  total: number
  page: number
  size: number
}

// ==================== 通用 ====================
export interface UserBrief {
  id: number; username: string; avatar: string
}
export interface PageResult<T> {
  list: T[]; total: number; page: number; size: number
}

// ==================== 评论 ====================
export interface CommentItem {
  id: number; user: UserBrief; checkInId: number
  parentId: number | null; replyToUser?: UserBrief
  content: string; likeCount: number; hasLiked: boolean
  status: number; replies: CommentItem[]; createdAt: string
}
export interface CommentCreateForm {
  checkInId: number; parentId?: number; replyToId?: number
  replyToUserId?: number; content: string
}

// ==================== 关注 ====================
export interface FollowUser extends UserBrief {
  isMutual: boolean; followedAt: string
}

// ==================== 通知 ====================
export type NotificationType = 'LIKE' | 'COMMENT' | 'REPLY' | 'FOLLOW' | 'SYSTEM' | 'ACHIEVEMENT'
export interface NotificationItem {
  id: number; type: NotificationType; fromUser?: UserBrief
  targetType?: string; targetId?: number; content: string
  isRead: boolean; createdAt: string
}

// ==================== 话题 ====================
export interface TopicItem {
  id: number; name: string; icon?: string; description?: string
  checkInCount: number; viewCount: number; isHot: boolean
}

// ==================== 成就 ====================
export interface AchievementItem {
  id: number; code: string; name: string; description: string
  icon?: string; category: string; level: number
  progress: number; isUnlocked: boolean; unlockedAt?: string; pointsReward: number
}
export interface ChallengeItem {
  id: number; achievement: AchievementItem; progress: number
  target: number; isCompleted: boolean; startDate: string; endDate: string
}

// ==================== 游记 ====================
export interface TravelNote {
  id: number; author: UserBrief; title: string; summary?: string
  coverImage?: string; content: string; city?: string; tags: string[]
  checkInPoints: CheckIn[]; viewCount: number; likeCount: number
  collectCount: number; commentCount: number; hasLiked: boolean
  hasCollected: boolean; createdAt: string; updatedAt: string
}
export interface TravelNoteBrief {
  id: number; author: UserBrief; title: string; summary?: string
  coverImage?: string; city?: string; viewCount: number
  likeCount: number; createdAt: string
}

// ==================== 积分 ====================
export interface UserPoints {
  totalPoints: number; currentPoints: number
  level: number; levelName: string; nextLevelPoints: number
}
export interface PointRecordItem {
  id: number; type: string; points: number; description: string
  balanceAfter: number; createdAt: string
}

// ==================== 商家 ====================
export interface MerchantPosition {
  id: number; name: string; category: string; address?: string
  longitude: number; latitude: number; rating: number
  priceLevel: number; tags?: string[]; coverImage?: string
  distance: number; phone?: string; businessHours?: string; description?: string
}

// ==================== 行程规划 ====================
export interface TripPlanBrief {
  id: number; title: string; city?: string; coverImage?: string
  startDate?: string; endDate?: string; totalDays: number; status: number
  createdAt: string
}
export interface TripPlan extends TripPlanBrief {
  description?: string; isPublic: boolean; days: TripDay[]
}
export interface TripDay {
  id: number; dayNumber: number; title?: string; date?: string; pois: TripPOI[]
}
export interface TripPOI {
  id: number; name: string; longitude: number; latitude: number
  address?: string; category?: string; notes?: string
  durationMinutes: number; sortOrder: number
}

// ==================== 数据可视化 ====================
export interface UserReport {
  totalCheckIns: number; totalCities: number; totalLikes: number
  totalDays: number; longestStreak: number
  mostActiveMonth: string; mostActiveCity: string; favoriteCategory: string
}
export interface HeatmapPoint { date: string; count: number }
export interface ChartData { labels: string[]; values: number[]; seriesName: string }
