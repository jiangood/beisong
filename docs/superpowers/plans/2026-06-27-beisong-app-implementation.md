# Beisong (背诵) Android App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a minimal Android recitation app that reads built-in txt files and displays them segment-by-segment with eye-care green background.

**Architecture:** Single-Activity app with Jetpack Compose + Navigation Compose. Two screens: file list and reader. FileRepository reads from assets/ and splits by blank lines. ViewModel holds UI state.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, ViewModel + StateFlow, Material3.

---

### Task 1: Scaffold Android project structure

**Files:**
- Create: `build.gradle.kts` (root)
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/proguard-rules.pro` (empty)

- [ ] **Step 1: Create root build.gradle.kts**

```kotlin
// build.gradle.kts (root)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

- [ ] **Step 2: Create settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "beisong"
include(":app")
```

- [ ] **Step 3: Create gradle.properties**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Create version catalog libs.versions.toml**

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"
navigationCompose = "2.8.5"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 5: Create app/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.beisong.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.beisong.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
}
```

- [ ] **Step 6: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Beisong">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Beisong">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 7: Create proguard-rules.pro** (empty file)

- [ ] **Step 8: Commit**

```bash
git add build.gradle.kts settings.gradle.kts gradle.properties gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml app/proguard-rules.pro
git commit -m "chore: scaffold Android project with Compose"
```

---

### Task 2: Create theme resources and sample txt assets

**Files:**
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/assets/周易.txt`
- Create: `app/src/main/assets/道德经.txt`
- Create: `app/src/main/assets/诗经.txt`

- [ ] **Step 1: Create colors.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="eye_green_bg">#C7EDCC</color>
    <color name="eye_green_surface">#B8E0BB</color>
    <color name="text_primary">#333333</color>
    <color name="text_secondary">#666666</color>
</resources>
```

- [ ] **Step 2: Create themes.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Beisong" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@color/eye_green_bg</item>
        <item name="android:navigationBarColor">@color/eye_green_bg</item>
    </style>
</resources>
```

- [ ] **Step 3: Create strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">背诵</string>
    <string name="no_content">暂无内容</string>
    <string name="no_texts">暂无文本</string>
    <string name="prev_segment">上一段</string>
    <string name="next_segment">下一段</string>
    <string name="segment_info">第 %1$d / %2$d 段</string>
</resources>
```

- [ ] **Step 4: Create 周易.txt** (excerpt with blank-line segments)

```
乾卦第一

乾：元，亨，利，贞。

初九：潜龙，勿用。
九二：见龙在田，利见大人。
九三：君子终日乾乾，夕惕若厉，无咎。
九四：或跃在渊，无咎。
九五：飞龙在天，利见大人。
上九：亢龙有悔。
用九：见群龙无首，吉。

坤卦第二

坤：元，亨，利牝马之贞。君子有攸往，先迷后得主，利西南得朋，东北丧朋。安贞吉。

初六：履霜，坚冰至。
六二：直方大，不习无不利。
六三：含章可贞，或从王事，无成有终。
六四：括囊，无咎无誉。
六五：黄裳，元吉。
上六：龙战于野，其血玄黄。
用六：利永贞。

屯卦第三

屯：元，亨，利，贞。勿用有攸往，利建侯。

初九：磐桓，利居贞，利建侯。
六二：屯如邅如，乘马班如。匪寇婚媾，女子贞不字，十年乃字。
六三：即鹿无虞，惟入于林中，君子几不如舍，往吝。
六四：乘马班如，求婚媾，往吉，无不利。
九五：屯其膏，小贞吉，大贞凶。
上六：乘马班如，泣血涟如。

蒙卦第四

蒙：亨。匪我求童蒙，童蒙求我。初筮告，再三渎，渎则不告。利贞。

初六：发蒙，利用刑人，用说桎梏，以往吝。
九二：包蒙，吉。纳妇，吉。子克家。
六三：勿用取女，见金夫，不有躬，无攸利。
六四：困蒙，吝。
六五：童蒙，吉。
上九：击蒙，不利为寇，利御寇。
```

- [ ] **Step 5: Create 道德经.txt** (excerpt with blank-line segments)

```
第一章

道可道，非常道；名可名，非常名。
无名天地之始，有名万物之母。
故常无欲，以观其妙；常有欲，以观其徼。
此两者同出而异名，同谓之玄，玄之又玄，众妙之门。

第二章

天下皆知美之为美，斯恶已；皆知善之为善，斯不善已。
故有无相生，难易相成，长短相形，高下相倾，音声相和，前后相随。
是以圣人处无为之事，行不言之教，万物作焉而不辞，生而不有，为而不恃，功成而弗居。
夫唯弗居，是以不去。

第三章

不尚贤，使民不争；不贵难得之货，使民不为盗；不见可欲，使民心不乱。
是以圣人之治，虚其心，实其腹，弱其志，强其骨。
常使民无知无欲，使夫智者不敢为也。
为无为，则无不治。

第四章

道冲而用之或不盈，渊兮似万物之宗。
挫其锐，解其纷，和其光，同其尘。
湛兮似或存，吾不知谁之子，象帝之先。

第五章

天地不仁，以万物为刍狗；圣人不仁，以百姓为刍狗。
天地之间，其犹橐籥乎？虚而不屈，动而愈出。
多言数穷，不如守中。
```

- [ ] **Step 6: Create 诗经.txt** (excerpt with blank-line segments)

```
关雎·周南

关关雎鸠，在河之洲。
窈窕淑女，君子好逑。

参差荇菜，左右流之。
窈窕淑女，寤寐求之。

求之不得，寤寐思服。
悠哉悠哉，辗转反侧。

参差荇菜，左右采之。
窈窕淑女，琴瑟友之。

参差荇菜，左右芼之。
窈窕淑女，钟鼓乐之。

蒹葭·秦风

蒹葭苍苍，白露为霜。
所谓伊人，在水一方。
溯洄从之，道阻且长。
溯游从之，宛在水中央。

蒹葭萋萋，白露未晞。
所谓伊人，在水之湄。
溯洄从之，道阻且跻。
溯游从之，宛在水中坻。

蒹葭采采，白露未已。
所谓伊人，在水之涘。
溯洄从之，道阻且右。
溯游从之，宛在水中沚。

桃夭·周南

桃之夭夭，灼灼其华。
之子于归，宜其室家。

桃之夭夭，有蕡其实。
之子于归，宜其家室。

桃之夭夭，其叶蓁蓁。
之子于归，宜其家人。
```

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/values/ app/src/main/assets/
git commit -m "feat: add theme resources and sample txt assets"
```

---

### Task 3: Implement FileRepository

**Files:**
- Create: `app/src/main/java/com/beisong/app/data/FileRepository.kt`

- [ ] **Step 1: Create FileRepository.kt**

```kotlin
package com.beisong.app.data

import android.content.Context

class FileRepository(private val context: Context) {

    fun listTextFiles(): List<String> {
        return context.assets.list("")?.filter { it.endsWith(".txt") }?.sorted() ?: emptyList()
    }

    fun loadSegments(fileName: String): List<String> {
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return text.split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun displayName(fileName: String): String {
        return fileName.removeSuffix(".txt")
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/beisong/app/data/FileRepository.kt
git commit -m "feat: add FileRepository for assets reading and text segmentation"
```

---

### Task 4: Implement FileListScreen + ViewModel

**Files:**
- Create: `app/src/main/java/com/beisong/app/ui/filelist/FileListViewModel.kt`
- Create: `app/src/main/java/com/beisong/app/ui/filelist/FileListScreen.kt`

- [ ] **Step 1: Create FileListViewModel.kt**

```kotlin
package com.beisong.app.ui.filelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beisong.app.data.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FileListUiState(
    val files: List<Pair<String, String>> = emptyList(), // (fileName, displayName)
    val isLoading: Boolean = true
)

class FileListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)

    private val _uiState = MutableStateFlow(FileListUiState())
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()

    init {
        loadFiles()
    }

    private fun loadFiles() {
        viewModelScope.launch {
            val files = repository.listTextFiles().map { fileName ->
                fileName to repository.displayName(fileName)
            }
            _uiState.value = FileListUiState(files = files, isLoading = false)
        }
    }
}
```

- [ ] **Step 2: Create FileListScreen.kt**

```kotlin
package com.beisong.app.ui.filelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    onFileClick: (String) -> Unit,
    viewModel: FileListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("背诵") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFC7EDCC),
                    titleContentColor = Color(0xFF333333)
                )
            )
        },
        containerColor = Color(0xFFC7EDCC)
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF333333))
            }
        } else if (uiState.files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无文本", color = Color(0xFF666666), fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFC7EDCC)),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.files) { (fileName, displayName) ->
                    Text(
                        text = displayName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFileClick(fileName) }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        color = Color(0xFF333333),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = Color(0xFFA8D8A8),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/beisong/app/ui/filelist/
git commit -m "feat: add file list screen with ViewModel"
```

---

### Task 5: Implement ReaderScreen + ViewModel

**Files:**
- Create: `app/src/main/java/com/beisong/app/ui/reader/ReaderViewModel.kt`
- Create: `app/src/main/java/com/beisong/app/ui/reader/ReaderScreen.kt`

- [ ] **Step 1: Create ReaderViewModel.kt**

```kotlin
package com.beisong.app.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beisong.app.data.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReaderUiState(
    val fileName: String = "",
    val segments: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val fontSize: Float = 18f,
    val totalSegments: Int = 0
)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadFile(fileName: String) {
        viewModelScope.launch {
            val segments = repository.loadSegments(fileName)
            _uiState.value = ReaderUiState(
                fileName = repository.displayName(fileName),
                segments = segments,
                currentIndex = 0,
                totalSegments = segments.size
            )
        }
    }

    fun nextSegment() {
        val state = _uiState.value
        if (state.currentIndex < state.totalSegments - 1) {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1)
        }
    }

    fun prevSegment() {
        val state = _uiState.value
        if (state.currentIndex > 0) {
            _uiState.value = state.copy(currentIndex = state.currentIndex - 1)
        }
    }

    fun setFontSize(size: Float) {
        _uiState.value = _uiState.value.copy(fontSize = size.coerceIn(14f, 32f))
    }
}
```

- [ ] **Step 2: Create ReaderScreen.kt**

```kotlin
package com.beisong.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    fileName: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    LaunchedEffect(fileName) {
        viewModel.loadFile(fileName)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.fileName) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("< 返回", color = Color(0xFF333333), fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFC7EDCC),
                    titleContentColor = Color(0xFF333333)
                )
            )
        },
        bottomBar = {
            ReaderBottomBar(
                currentIndex = uiState.currentIndex,
                totalSegments = uiState.totalSegments,
                fontSize = uiState.fontSize,
                onPrev = { viewModel.prevSegment() },
                onNext = { viewModel.nextSegment() },
                onFontSizeChange = { viewModel.setFontSize(it) }
            )
        },
        containerColor = Color(0xFFC7EDCC)
    ) { padding ->
        if (uiState.segments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无内容", color = Color(0xFF666666), fontSize = 16.sp)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFC7EDCC))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = uiState.segments.getOrElse(uiState.currentIndex) { "" },
                    color = Color(0xFF333333),
                    fontSize = uiState.fontSize.sp,
                    lineHeight = (uiState.fontSize * 1.8).sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun ReaderBottomBar(
    currentIndex: Int,
    totalSegments: Int,
    fontSize: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onFontSizeChange: (Float) -> Unit
) {
    Surface(
        color = Color(0xFFB8E0BB),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Font size slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("A", color = Color(0xFF333333), fontSize = 12.sp)
                Slider(
                    value = fontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 14f..32f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF333333),
                        activeTrackColor = Color(0xFF333333),
                        inactiveTrackColor = Color(0xFFA8D8A8)
                    )
                )
                Text("A", color = Color(0xFF333333), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPrev,
                    enabled = currentIndex > 0
                ) {
                    Text(
                        "< 上一段",
                        color = if (currentIndex > 0) Color(0xFF333333) else Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "第 ${currentIndex + 1} / $totalSegments 段",
                    color = Color(0xFF333333),
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = onNext,
                    enabled = currentIndex < totalSegments - 1
                ) {
                    Text(
                        "下一段 >",
                        color = if (currentIndex < totalSegments - 1) Color(0xFF333333) else Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/beisong/app/ui/reader/
git commit -m "feat: add reader screen with ViewModel"
```

---

### Task 6: Implement Navigation and MainActivity

**Files:**
- Create: `app/src/main/java/com/beisong/app/navigation/NavGraph.kt`
- Create: `app/src/main/java/com/beisong/app/MainActivity.kt`

- [ ] **Step 1: Create NavGraph.kt**

```kotlin
package com.beisong.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.beisong.app.ui.filelist.FileListScreen
import com.beisong.app.ui.reader.ReaderScreen
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val FILE_LIST = "file_list"
    const val READER = "reader/{fileName}"
    fun reader(fileName: String): String = "reader/${URLEncoder.encode(fileName, "UTF-8")}"
}

@Composable
fun BeisongNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.FILE_LIST) {
        composable(Routes.FILE_LIST) {
            FileListScreen(
                onFileClick = { fileName ->
                    navController.navigate(Routes.reader(fileName))
                }
            )
        }
        composable(
            route = Routes.READER,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = URLDecoder.decode(
                backStackEntry.arguments?.getString("fileName") ?: "",
                "UTF-8"
            )
            ReaderScreen(
                fileName = fileName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **Step 2: Create MainActivity.kt**

```kotlin
package com.beisong.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.beisong.app.navigation.BeisongNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFC7EDCC)),
                color = Color(0xFFC7EDCC)
            ) {
                val navController = rememberNavController()
                BeisongNavGraph(navController = navController)
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/beisong/app/navigation/ app/src/main/java/com/beisong/app/MainActivity.kt
git commit -m "feat: add navigation graph and main activity"
```

---

### Self-Review Checklist

1. **Spec coverage:** Every spec requirement has a corresponding task.
   - Built-in txt assets → Task 2 (周易.txt, 道德经.txt, 诗经.txt)
   - Segment by blank lines → Task 3 (Repository.split)
   - File list screen → Task 4
   - Reader screen with segment display → Task 5
   - Font size slider → Task 5 (ReaderScreen bottom bar)
   - Eye-care green (#C7EDCC) → Task 2 colors.xml, applied in all screens
   - Navigation between screens → Task 6
   - Package structure matches spec → All tasks follow planned package layout

2. **Placeholder scan:** No TBD, TODO, or "implement later" found. All code is complete.

3. **Type consistency:** ReaderUiState field names match between ViewModel and Screen. FileRepository methods are consistent across Tasks 3-5.
