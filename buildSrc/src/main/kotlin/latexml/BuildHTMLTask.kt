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

    @Option(description = "the name of the target xml/html (without extension)")
    var target: String = "index"

    @Option(description = "If not empty, use target tex file as a template and substitute strings with this parameters using groovy templete engine")
    var parameters: Map<String, String> = emptyMap()

    @Option(description = "List of js imports")
    var js: MutableList<String> = arrayListOf("http://fred-wang.github.io/mathjax.js/mpadded-min.js")

    @Option(description = "List of css imports")
    var css: MutableList<String> = ArrayList()


    @OutputDirectory
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


        val xmlLine = project.platformCommand +
                listOf(
                        "latexml",
                        "--destination=\"${convertPath(project, xmlFile.absolutePath)}\"",
                        "--inputencoding=utf8",
//                "--includestyles",
                        "\"${convertPath(project, sourceFile.absolutePath)}\""
                )

        logger.lifecycle("Preparing xml")
        logger.lifecycle("Using command line for latexml $target: ${xmlLine.joinToString(" ")}")
        project.exec {
            workingDir = project.rootDir
            errorOutput = FileOutputStream(File(logDir, "$target.xml.log"))
            //TODO убрать первый аргумент для линукса
            commandLine = xmlLine
        }

        htmlDir.mkdirs()

        val jsArray = js.map { "--javascript='$it'" }.toTypedArray()
        val cssArray = css.map { "--css='$it'" }.toTypedArray()

        val htmlLine = project.platformCommand + listOf(
                "latexmlpost",
                "--destination=${convertPath(project, htmlDir.toString())}/$target.html",
                "--format=html5",
//                "--split",
//                "--splitat=section",
                *jsArray,
                *cssArray,
                convertPath(project, xmlFile.absolutePath)
        )

        logger.lifecycle("Command line for latexmlpost $target: ${htmlLine.joinToString(" ")}")

        project.exec {
            workingDir = project.rootDir
            errorOutput = FileOutputStream(File(logDir, "$target.html.log"))
            commandLine = htmlLine

        }
    }
}
