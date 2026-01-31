import { get, post } from '@/utils/request'
import type {
  PageResult,
  MemberQueryRequest,
  MemberListVO,
  MemberExtendRequest,
} from '@/types'

// 会员列表
export function getMemberList(params: MemberQueryRequest): Promise<PageResult<MemberListVO>> {
  return get('/admin/member/list', params)
}

// 会员续期
export function extendMember(data: MemberExtendRequest): Promise<void> {
  return post('/admin/member/extend', data)
}

// 会员统计
export function getMemberStats(): Promise<any> {
  return get('/admin/member/stats')
}
