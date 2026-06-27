# 长按选词翻译 - 重新设计

## 核心变更
将当前的"长按直接触发查字"改为：**标准文本选择 + 悬浮翻译按钮**

## 用户交互流程
1. 用户长按阅读文本 → 系统标准选择手柄出现
2. 用户拖拽手柄调整选区
3. 选区附近出现悬浮"翻译"胶囊按钮
4. 点击"翻译" → 提取选区首个汉字 → 查字典 → 弹窗显示字条 + 词组候选
5. 点击词组候选 → 依次展示各字字条（或合并显示）
6. 点击弹窗外部或"关闭" → 关闭弹窗，保留选区

## 技术方案

### 1. ReaderScreen.kt
- 保留 `SelectionContainer { Text(...) }` 标准选择
- 新增 `selectedText` 状态记录当前选中文本
- 用 `Modifier.pointerInput` + `detectTapGestures` 检测点击，配合 `TextLayoutResult.getOffsetForPosition` 定位字符
- 选中文本变化时更新 `selectedText` 状态
- 有选中文本时，在选区附近渲染悬浮"翻译"按钮（用 `Popup` 或 `Box` + 绝对定位）
- 点击悬浮按钮 → `viewModel.onTextSelected(selectedText)`

### 2. ReaderViewModel.kt
- 保持现有 `onTextSelected(text: String)` 逻辑不变
- 提取首个汉字 → 查字典 → 计算词组候选 → 更新 `selectedChar` + `wordCandidates`

### 3. CharLookupDialog.kt
- 保持现有缩小字号设计
- 词组候选区点击回调：点击词组 → 依次查每个字（或合并展示）

## 文件变更
| 文件 | 变更 |
|------|------|
| `ReaderScreen.kt` | 重写选择检测、悬浮按钮渲染、移除长按直接触发 |
| `ReaderViewModel.kt` | 保持现有 `onTextSelected`，无需改动 |
| `CharLookupDialog.kt` | 词组候选点击回调实现 |
| `ReaderViewModel.kt` | `ReaderUiState` 已包含 `wordCandidates`、`selectedChar` |

## 关键实现细节

### 选区文本获取
```kotlin
var selectedText by remember { mutableStateOf("") }
var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

// Text 内部
onTextLayout = { textLayoutResult = it }

// 监听选择变化 - 用 pointerInput 检测点击获取选区
// 或者用 SelectionContainer 的 onSelectionChanged（需 Compose 1.7+）
// 兼容方案：pointerInput + getOffsetForPosition 取光标位置推算
```

### 悬浮按钮定位
用 `TextLayoutResult.getBoundingBox(charOffset)` 获取选区矩形，在其上方/下方显示 `Popup` 或绝对定位 `Box`。

### 词组候选点击
```kotlin
candidates.forEach { word ->
    Surface(onClick = { viewModel.onWordCandidateClicked(word) }) { ... }
}
```
ViewModel 新增 `onWordCandidateClicked(word: String)`：依次查词中每字，更新 `selectedChar` 轮播，或合并展示。

## 验收标准
1. 长按文本 → 系统选择手柄正常出现，可拖拽调整
2. 有选区时 → 选区上方显示"翻译"胶囊按钮
3. 点击"翻译" → 弹窗显示首字字条 + 词组候选
4. 点击词组候选 → 切换显示该词各字字条
5. 切换段落/关闭弹窗 → 清空选中状态
6. `./gradlew.bat assembleDebug` 编译通过