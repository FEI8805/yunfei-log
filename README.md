# 云飞日志 · Android

每日工作记录 App。每晚定点提醒录入，按夏冬时令与节假日规则自动计算工时。

---

## 一、最快拿到 APK：用 GitHub 云端打包（不用装任何软件）

1. 打开 https://github.com/new 新建一个仓库，比如叫 `yunfei-log`，**选 Private 或 Public 都行**，不要勾选任何初始化文件。
2. 把本目录 `yunfei-log/` 里的**全部文件**上传到仓库（网页上点 `Add file → Upload files`，把整个文件夹拖进去即可，`.github` 目录也要一起传）。
3. 上传完成后进入仓库的 **Actions** 标签页，会自动开始一次 `Build APK` 构建；如果没有自动开始，点左侧 `Build APK` → 右侧 `Run workflow`。
4. 等约 3–5 分钟，构建成功后会出现在列表里，点进去，页面底部 **Artifacts** 区域有 `yunfei-log-apk`，下载得到一个 zip，解压里面就是 `app-release.apk`。
5. 把这个 APK 发到手机上（微信文件传输助手 / QQ / 数据线都行），点击安装。首次安装需要在手机上允许「安装未知来源应用」。

> 以后想改功能，只要把新代码传上去，Actions 会自动重新出包，全程不用碰电脑上的开发环境。

## 二、或者用 Android Studio 本地打包

1. 安装 Android Studio（官网 https://developer.android.com/studio ，约 1GB）。
2. `File → Open`，选择本目录 `yunfei-log`。
3. 第一次打开会提示下载 Gradle 8.7 和 Android SDK 35，同意即可（需要联网，约 1–2GB）。
4. 连上手机（开启开发者选项 + USB 调试），点绿色三角直接安装运行；或菜单 `Build → Build Bundle(s) / APK(s) → Build APK(s)` 生成 APK，产物在 `app/build/outputs/apk/release/`。

> 本工程没有附带 `gradlew` 脚本（仓库里不方便直接放二进制文件）。
> 如果 Android Studio 提示缺少 Gradle Wrapper，在 `Settings → Build → Build Tools → Gradle` 里把「Use Gradle from」改成 **Specified location** 指向 IDE 自带的 Gradle，或先在有网环境执行一次 `gradle wrapper`。用方案一（GitHub）则完全不用管这件事。

## 三、安装后必做的两件事

国产手机系统会限制后台闹钟，不设置的话提醒可能不生效：

1. 打开 App → 设置 → 按提示开启 **通知权限** 和 **精确闹钟**。
2. 在系统设置里把「云飞日志」的**省电策略设为无限制**、**允许自启动**（设置页里的「后台运行 / 自启动」按钮可直接跳到应用详情页）。

## 四、工时规则

| 场景 | 计算方式 |
| --- | --- |
| 正常工作日 + 正常出勤 + 有工作内容 | 计标准工时（夏 9.5h / 冬 9.0h） |
| 正常工作日但没有记录 | 不计工时 |
| 请假 / 休假 / 调休 | 不计正常工时，加班照算 |
| 周末、法定节假日有录入内容 | 自动按当季标准工时折算为加班 |
| 加班时长 | 加班框填写时长 + 休息日自动折算时长 |

- 夏时令：5月1日 – 9月30日，9:00 – 18:30，标准 9.5 小时
- 冬时令：10月1日 – 次年4月30日，9:00 – 18:00，标准 9.0 小时
- 调休上班日按正常工作日计算

## 五、需要你后续维护的两处

1. **节假日表**：`app/src/main/java/com/yunfei/log/logic/WorkHours.kt` 里的 `HOLIDAYS` 和 `MAKEUP`。目前内置的是 **2026 年示例数据**，每年国务院办公厅会发布新的放假安排，需要按通知核对更新（主要是春节和国庆的调休上班日）。
2. **启动页字体**：已改用你提供的 **汉仪雪君体繁**，字体文件已放进 `app/src/main/res/font/xuejunti.ttf`（约 6.3 MB，会一并打进 APK）。
   想再换字体的话，把新的 ttf 覆盖这个文件，或在 `ui/SplashScreen.kt` 里改 `FontFamily(Font(R.font.xxx))` 的资源名即可。
   注意字体资源名只能用小写字母、数字和下划线。

## 六、工程结构

| 路径 | 作用 |
| --- | --- |
| `app/src/main/java/com/yunfei/log/data/` | 数据模型与本地存储（SharedPreferences + JSON） |
| `.../logic/WorkHours.kt` | 工时规则引擎、节假日表 |
| `.../logic/Dates.kt` | 日期工具 |
| `.../alarm/` | 闹钟调度、通知、通知栏快捷回复、开机重排 |
| `.../ui/` | 启动页与四个页面（今日 / 历史 / 统计 / 设置） |
| `../res/` | 图标、字符串、主题 |

## 七、当前版本

- 包名 `com.yunfei.log`，版本 1.0，minSdk 26（Android 8.0），targetSdk 35
- 数据全部本地存储，不联网、不上传
- release 包使用 debug 签名，可直接安装（个人自用足够；上架应用商店需换成正式签名）
