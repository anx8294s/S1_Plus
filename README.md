## S1_Plus
Saraba1st论坛的安卓客户端

网络通信使用 Asynchronous Http Client 实现

图片下载采用 Volley, 图片缓存为 LruCache + DiskLruCache 的双重缓存

使用 largeHeap 避免加载大量图片的帖子时出现 OOM

使用 Jsoup分析返回的网络请求

缓存了论坛列表和帖子列表, 如果内存有历史记录就从历史记录读取

新增加 rebuild 分支, 使用 butterknife, eventbus 重构了代码

使用 toolbar 替换了 actionbar, 添加了导航抽屉

###应用实现的功能

* 主论坛及子论坛及帖子列表的浏览,刷新, 页尾自动加载

* 帖子列表长按进入帖子最新页

* 主题帖图片的异步加载, 主题帖的收藏, 回复, 页尾自动加载

* 主题帖下拉读取上一页, 末尾页快速滑动加载最新回复

* 论坛的登录及查看已有收藏
