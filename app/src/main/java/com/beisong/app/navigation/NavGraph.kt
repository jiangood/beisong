package com.beisong.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.beisong.app.ui.filelist.FileListScreen
import com.beisong.app.ui.reader.ReaderScreen

object Routes {
    const val FILE_LIST = "file_list"
    const val READER = "reader/{fileName}"
    fun reader(fileName: String): String = "reader/$fileName"
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
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            ReaderScreen(
                fileName = fileName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
