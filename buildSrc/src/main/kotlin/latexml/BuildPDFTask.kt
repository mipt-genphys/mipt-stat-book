package latexml

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.io.FileOutputStream

open class BuildPDFTask : DefaultTask() {
    @Option(description = "Source file name or template without `.tex` suffix")
    lateinit var source: String

    @Option(description = "the name of the target pdf")
    var target: String? = null

    var parameters: Map<String, String> = emptyMap()

    var logDir = File(project.buildDir, "logs")

    @OutputDirectory
    var outputDir = File(project.buildDir, "pdf")

    @TaskAction
    fun run() {
        val target = target ?: source

        val texFile = project.file("$source.tex")
        val sourceFile = loadTex(project, texFile, target, parameters)

        outputDir.mkdirs()
        logDir.mkdirs()

        val line = project.platformCommand +
                listOf(
                    "lualatex", 
                    "--synctex=1", 
                    "--interaction=nonstopmode", 
                    "--output-directory=$outputDir", 
                    "--shell-escape", 
                    "--job-name=\"$target\"", 
                    sourceFile.absolutePath
                    )

        logger.lifecycle("Using command line for fragment $target: ${line.joinToString(" ")}")

        repeat(2) {
            logger.lifecycle("Lualatex iteration $it")
            project.exec {
                workingDir = project.rootDir
                standardOutput = FileOutputStream(File(logDir, "${target}.log"))
                commandLine = line
            }
        }
        //TODO either use texify or latexmk
    }
}