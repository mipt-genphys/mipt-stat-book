package latexml

import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project
import java.io.File

/**
 * На Windows заменяет пути на те, что пойдут в wsl, на линуксе ничего не делает
 */
fun convertPath(project: Project, path: String): String {
    if (project.platform == "wsl") {
        return path.replace("([A-Z]):".toRegex()) { "/mnt/${it.groupValues[1]}" }.replace("\\\\", "/")
    } else {
        return path
    }
}

/**
 * Load tex file or use it as template if needed
 * @param project
 * @param source
 * @param target
 * @param parameters
 * @return
 */
fun loadTex(project: Project, template: File, target: String, parameters: Map<String, String>): File {

    if (parameters.isEmpty()) {
        //No templating is needed, using source file
        return template
    } else {
        val templateText = template.readText(Charsets.UTF_8).replace("\\", "\\\\")
        val input = SimpleTemplateEngine().createTemplate(templateText).make(parameters + ("fragment" to "${target}.tex")).toString()
        val tempFile = File(project.buildDir, "${target}_tmp.tex")

        tempFile.writeText(input, Charsets.UTF_8)
        return tempFile
    }
}

val Project.platform: String
    get() = findProperty("platform").toString()

val Project.platformCommand: List<String>
    get() = when (project.platform) {
        "wsl" -> listOf("wsl")
        "linux" -> listOf("bash")
        else -> emptyList()
    }