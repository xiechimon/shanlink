import http from '../axios'
export default {
  queryPage(data) {
    return http({
      url: '/v1/link/page',
      method: 'get',
      params: data
    })
  },
  addSmallLink(data) {
    return http({
      url: '/v1/link',
      method: 'post',
      data
    })
  },
  addLinks(data) {
    return http({
      responseType: 'arraybuffer',
      url: '/v1/link/batch',
      method: 'post',
      data
    })
  },
  editSmallLink(data) {
    return http({
      url: '/v1/link',
      method: 'put',
      data
    })
  },
  // 通过链接查询标题
  queryTitle(data) {
    return http({
      method: 'get',
      url: '/v1/title',
      params: data
    })
  },
  // 移动到回收站
  toRecycleBin(data) {
    return http({
      url: '/v1/recycle-bin/save',
      method: 'post',
      data
    })
  },
  // 查询回收站数据
  queryRecycleBin(data) {
    return http({
      url: '/v1/recycle-bin/page',
      method: 'get',
      params: data
    })
  },
  // 恢复短链接
  recoverLink(data) {
    return http({
      method: 'post',
      url: '/v1/recycle-bin/recover',
      data
    })
  },
  removeLink(data) {
    return http({
      method: 'post',
      url: '/v1/recycle-bin/remove',
      data
    })
  },
  // 查询单链的图表数据
  queryLinkStats(data) {
    return http({
      method: 'get',
      params: data,
      url: '/v1/stats'
    })
  },
  // 查询单链的访问记录
  queryLinkTable(data) {
    return http({
      method: 'get',
      params: data,
      url: '/v1/stats/access-record'
    })
  }
}
