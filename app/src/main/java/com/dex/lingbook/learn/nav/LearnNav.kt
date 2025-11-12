package com.dex.lingbook.learn.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dex.lingbook.learn.model.Learn
import com.dex.lingbook.learn.model.LessonType
import com.dex.lingbook.learn.ui.*
import com.dex.lingbook.learn.ui.lessons.LessonScreen
import com.dex.lingbook.learn.viewmodel.LearnViewModel

@Composable
fun LearnNavHost(startDestination: String = "learn/skillmap") {
    val nav = rememberNavController()
    val vm: LearnViewModel = viewModel()

    NavHost(navController = nav, startDestination = startDestination) {

        composable("learn/skillmap") {
            SkillMapScreen(
                vm = vm,
                onOpenLesson = { skill ->
                    vm.loadLessonsAndStart(skill) { first ->
                        val t = Uri.encode(first.title)
                        nav.navigate("learn/lesson/${skill.id}/${first.id}/$t/${first.type.name}")
                    }
                }
            )
        }

        dialog(
            route = "learn/lesson/{skillId}/{lessonId}/{lessonTitle}/{lessonType}",
            arguments = listOf(
                navArgument("skillId"){ type = NavType.StringType },
                navArgument("lessonId"){ type = NavType.StringType },
                navArgument("lessonTitle"){ type = NavType.StringType },
                navArgument("lessonType"){ type = NavType.StringType },
            ),
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false,   // full width
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ){ back ->
            val skillId = back.arguments!!.getString("skillId")!!
            val lessonId = back.arguments!!.getString("lessonId")!!
            val lessonTitle = Uri.decode(back.arguments!!.getString("lessonTitle")!!)
            val lessonType = when (back.arguments!!.getString("lessonType")!!) {
                "WORD_ORDER"    -> LessonType.WORD_ORDER
                "LISTEN_CHOOSE" -> LessonType.LISTEN_CHOOSE
                "IMAGE_PICK"    -> LessonType.IMAGE_PICK
                else            -> LessonType.WORD_ORDER
            }
            val lesson = Learn(id = lessonId, skillId = skillId, title = lessonTitle, type = lessonType)

            LessonScreen(
                skillId = skillId,
                lesson  = lesson,
                vm      = vm,
                onExit  = { nav.popBackStack() },
                onOpenNext = { next ->
                    val enc = Uri.encode(next.title)
                    nav.navigate("learn/lesson/$skillId/${next.id}/$enc/${next.type.name}") {
                        popUpTo("learn/skillmap") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
