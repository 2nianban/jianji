# 简记 Android 基础版本

这是根据产品需求文档创建的 Kotlin + Jetpack Compose + Room 基础工程。

## 下载安装

前往 [GitHub Releases](https://github.com/2nianban/jianji/releases/latest) 下载最新的 `jianji-vX.Y.Z.apk`。Android 8.0 及以上设备可安装。

每个版本同时提供 `.sha256` 文件，可用于校验 APK 下载是否完整。

应用包名为 `io.github.nianban2.jianji`，用于后续版本持续升级。此前使用 `com.example.simpleledger` 包名的测试 APK 无法直接覆盖安装，请先导出账单备份再安装正式版本。

## 已实现

- 本地 Room 数据库保存账单
- 支出、收入、转账三种记录类型
- 首页本月支出、收入、结余汇总
- 最近账单列表
- 首页最近账单和账单列表均可完整编辑类型、金额、日期、分类、账户和备注
- 账单列表和删除操作
- 按分类统计本月支出
- 周、月、年和自定义日期区间统计
- 收入、支出使用同结构分类统计和多色 3D 扇形图
- 长按扇形可增厚外移，并通过引线显示分类总金额和占比
- 现金、银行卡、微信、支付宝账户选项
- 自定义收入/支出分类和自定义账户，本地持久化
- 手动调整本月结余，并自动生成可追溯的调整账单
- 自定义软件背景、按设备比例裁剪、背景遮罩和恢复默认
- 统一圆角用量面板风格和白色“简”字自适应图标
- 页面切换、记账页进入退出和自定义背景使用轻量状态动画
- 版本化 `.jianji` 账单备份的导入、导出、重复过滤和异常校验
- 通过安卓分享菜单发送账单，另一台已安装简记的设备可直接打开并确认导入
- Android 8.0 及以上配置

## 暂未实现

- 预算和通知
- 云同步、登录和多人账本

## 构建

使用 Android Studio 打开本目录，等待 Gradle 同步完成后运行 `app` 配置。

环境要求：

- JDK 17
- Android SDK 35
- Android Studio Hedgehog 或更新版本

本项目当前使用 Android SDK 35、Gradle 8.7 和 JDK 17 构建。

## 版本发布

版本号采用以下约定：

- Android `versionName` 使用纯数字版本，例如 `0.5.4`
- Git 标签和 GitHub Release 使用 `v` 前缀，例如 `v0.5.4`
- 发布附件命名为 `jianji-v0.5.4.apk`，同时提供同名 `.sha256` 校验文件

推送版本标签后，GitHub Actions 会自动构建 Release APK 并创建 GitHub Release：

```bash
git tag -a v0.5.4 -m "Release v0.5.4"
git push origin v0.5.4
```

Release 使用独立的正式签名密钥。密钥文件和 `signing.properties` 只保存在本机或 GitHub Actions Secrets 中，禁止提交到仓库。

GitHub Actions 需要配置以下 Secrets：`ANDROID_SIGNING_KEYSTORE_BASE64`、`ANDROID_SIGNING_STORE_PASSWORD`、`ANDROID_SIGNING_KEY_ALIAS`、`ANDROID_SIGNING_KEY_PASSWORD`。其中 keystore 使用 Base64 编码后保存。

本机正式密钥位于仓库外的 `../jianji-release-signing/`。请将该目录完整备份到安全的离线介质或密码管理器；丢失密钥后将无法为现有用户发布可覆盖升级的版本。`signing-credentials.txt` 保存 GitHub Secrets 所需的别名和密码，keystore 可在 PowerShell 中这样转换为 Base64：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("..\jianji-release-signing\jianji-release.jks")) | Set-Clipboard
```

本地发布构建可先加载仓库外的签名环境文件，再运行 `./gradlew assembleRelease`：

```powershell
. ..\jianji-release-signing\signing.env.ps1
./gradlew assembleRelease
```

## 开源参考

- [CanHub Android Image Cropper](https://github.com/CanHub/Android-Image-Cropper)，Apache-2.0，用于固定比例背景裁剪。
- [Android Compose Samples](https://github.com/android/compose-samples)，Apache-2.0，参考状态驱动的页面进入退出动画。
- [Now in Android](https://github.com/android/nowinandroid)，Apache-2.0，参考 Compose 单向状态与克制的内容转场方式。
- [Android FileProvider](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/core/core/src/main/java/androidx/core/content/FileProvider.java)，Apache-2.0，用于以临时读取权限安全分享备份文件。
- [Ivy Wallet](https://github.com/Ivy-Apps/ivy-wallet) 和 [My Expenses](https://github.com/mtotschnig/MyExpenses)，参考版本化备份、导入预览和用户确认流程；因 GPL 许可差异未复制其代码。

动画实现使用 AndroidX Compose 自带 API，没有复制第三方页面代码或引入大型动画框架。

## 代码结构

核心代码暂时集中在 `app/src/main/java/io/github/nianban2/jianji/MainActivity.kt`，便于第一轮原型快速验证。功能稳定后建议拆分为：

```text
data/        Room entity、DAO、database
feature/home 首页
feature/transaction 记账
feature/bills 账单
feature/statistics 统计
feature/settings 设置
```
