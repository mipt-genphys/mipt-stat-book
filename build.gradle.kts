import latexml.BuildHTMLTask
import latexml.BuildPDFTask

plugins {
    id("base")
}

task<BuildPDFTask>("buildPdf"){
    group = "pdf"
    source = "main"
}

task<BuildHTMLTask>("buildHtml"){
    group = "html"
    source = "main"
}

tasks.findByName("build")?.let {
    it.dependsOn(":buildPdf")
    it.dependsOn(":buildHtml")
}

