# Beisong (背诵) Android App - Design Spec

## 概述

极简 Android 背诵 App，内置多个 txt 经典文本（周易、道德经、诗经等），
按空行分段显示，类似读书软件的阅读体验，护眼配色。

## 技术栈

- Kotlin + Jetpack Compose
- Navigation Compose 页面路由
- ViewModel + StateFlow
- 最低 Android 版本: API 26 (Android 8.0)

## 导航结构

```
FileListScreen ──点击条目──→ ReaderScreen
     ↑                          │
     └────────返回──────────────┘
```

两页面，无其他分支。

## 页面设计

### FileListScreen (文件列表)

- 居中纵向列表
- 每项显示文件名（去掉 `.txt` 后缀，中文原名）
- 点击跳转到 ReaderScreen，传入文件名

### ReaderScreen (阅读器)

- **顶部**: TopAppBar 显示当前书名 + 系统返回按钮
- **中央**: 当前段文本，可滚动
- **底部**: 工具栏
  - `< 上一段` 按钮 | 段号显示 `第 3 / 24 段` | `下一段 >` 按钮
  - 字号滑块 (最小值 14sp, 最大值 32sp, 默认 18sp)

## 数据层

### FileRepository

- 从 `assets/` 读取 txt 文件列表
- 读取指定文件内容，按 `\n\n`（连续两个换行）切分为段
- 返回 `List<String>`

### 内置文本格式

```
乾卦第一
（内容...）

坤卦第二
（内容...）
```

空行 = 分段标记。

## 配色方案 (护眼)

| 角色 | 颜色 |
|------|------|
| 背景 | `#C7EDCC` (豆沙绿) |
| 文字 | `#333333` |
| TopAppBar / 按钮 | 同背景色，无突兀装饰 |
| 分割线/边框 | `#A8D8A8` (略深绿) |

## 包结构

```
com.beisong.app
├── MainActivity.kt
├── navigation/
│   └── NavGraph.kt
├── data/
│   └── FileRepository.kt
├── ui/
│   ├── filelist/
│   │   ├── FileListScreen.kt
│   │   └── FileListViewModel.kt
│   └── reader/
│       ├── ReaderScreen.kt
│       └── ReaderViewModel.kt
```

## 状态管理

### FileListViewModel

```kotlin
data class FileListUiState(
    val files: List<String> = emptyList(),     // txt 文件名列表
    val isLoading: Boolean = true
)
```

### ReaderViewModel

```kotlin
data class ReaderUiState(
    val fileName: String = "",
    val segments: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val fontSize: Float = 18f,
    val totalSegments: Int = 0
)
```

- `currentIndex`, `fontSize` 不持久化（极简，不增加数据库依赖）

## 边界情况

- 文件为空 → 显示 "暂无内容"
- 文件只有一个段 → 上一段/下一段按钮均 disabled
- 第一段时 → 上一段按钮 disabled
- 最后一段时 → 下一段按钮 disabled
- assets 中无 txt 文件 → 显示 "暂无文本"

## 非目标（明确不做）

- 不保存阅读进度
- 不支持书签
- 不支持搜索
- 不支持外部导入
- 无数据库
- 无网络请求
