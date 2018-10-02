package latexml

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.io.FileOutputStream

open class BuildPDFTask : DefaultTask() {

    enum class LatexEngine{
        LUALATEX,
        PDFLATEX,
        XELATEX
    }

    @Option(description = "Source file name or template without `.tex` suffix")
    lateinit var source: String

    @Option(description = "the name of the target pdf. By default take source name")
    var target: String? = null

    @Option(description = "If not empty, use target tex file as a template and substitute strings with this parameters using groovy templete engine")
    var parameters: Map<String, String> = emptyMap()

    @Option(description = "The latex engine to use")
    var latexEngine = LatexEngine.LUALATEX

    @Option(description = "Latex engine arguments")
    var latexParameters = listOf("--synctex=1", "--interaction=nonstopmode", "--shell-escape")

    @OutputDirectory
    val logDir = File(project.buildDir, "logs")

    @OutputDirectory
    val outputDir = File(project.buildDir, "pdf")

    @get:InputFiles
    val texFiles
        get() = project.rootDir.listFiles { file: File -> file.extension == "tex" || file.extension == "sty" || file.extension == "ltxml" }

    @get:InputDirectory
    val resources
        get() = File(project.rootDir, "images")

    @TaskAction
    fun run() {
        val target = target ?: source

        val texFile = project.file("$source.tex")
        val sourceFile = loadTex(project, texFile, target, parameters)

        outputDir.mkdirs()
        logDir.mkdirs()

        val line = project.platformCommand +
                listOf(
                        latexEngine.name.toLowerCase(),
                        "--output-directory=$outputDir",
                        "--job-name=\"$target\"",
                        *latexParameters.toTypedArray(),
                        sourceFile.absolutePath
                )

        logger.lifecycle("Using command line for fragment $target: ${line.joinToString(" ")}")

        repeat(2) {
            logger.lifecycle("Lualatex iteration $it")
            project.exec {
                workingDir = project.rootDir
                standardOutput = File(logDir, "$target.log").outputStream()
                commandLine = line
            }
        }
        //TODO either use texify or latexmk
    }
}