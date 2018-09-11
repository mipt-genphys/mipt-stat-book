package latexml

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.io.FileOutputStream

//TODO split into two tasks

open class BuildHTMLTask : DefaultTask() {
    @Option(description = "Source file name or template without `.tex` suffix")
    lateinit var source: String

    @Option(description = "the name of the target xml/html")
    var target: String? = null

    var parameters: Map<String, String> = emptyMap()

    val logDir = File(project.buildDir, "logs")

    @OutputDirectory
    val xmlDir = File(project.buildDir, "xml")

    @OutputDirectory
    val htmlDir = File(project.buildDir, "html")

    @TaskAction
    fun run() {
        val target = target ?: source

        val texFile = project.file("$source.tex")
        val sourceFile = loadTex(project, texFile, target, parameters)

        val xmlFile = File(xmlDir, "${target}.xml")

        logDir.mkdirs()
        xmlDir.mkdirs()

        val xmlLine = project.platformCommand + listOf("latexml",
                "--destination=\"${convertPath(project, xmlFile.absolutePath)}\"",
                "--inputencoding=utf8",
                "\"${convertPath(project, sourceFile.absolutePath)}\""
        )

        logger.lifecycle("Preparing xml")
        logger.lifecycle("Using command line for latexml $target: ${xmlLine.joinToString(" ")}")
        project.exec {
            workingDir = project.rootDir
            errorOutput = FileOutputStream(File(logDir, "${target}.xml.log"))
            //TODO убрать первый аргумент для линукса
            commandLine = xmlLine
        }

        htmlDir.mkdirs()

        val htmlLine =  project.platformCommand + listOf(
                "latexmlpost",
                "--destination=${convertPath(project, htmlDir.toString())}/${xmlFile.name.replaceFirst("\\.[^.]+$".toRegex(), "")}.html",
                "--format=html5",
                "--split",
                "--splitat=section",
                "--javascript='http://fred-wang.github.io/mathjax.js/mpadded-min.js'",
                "--css=labnik.css",
                convertPath(project, xmlFile.absolutePath)
        )

        logger.lifecycle("Command line for latexmlpost $target: ${htmlLine.joinToString(" ")}")

        project.exec {
            workingDir = project.rootDir
            errorOutput = FileOutputStream(File(logDir, "${target}.html.log"))
            commandLine = htmlLine

        }
    }
}
