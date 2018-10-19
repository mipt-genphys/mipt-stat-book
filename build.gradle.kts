import latexml.BuildHTMLTask
import latexml.BuildPDFTask

plugins {
    id("base")
    id("org.hidetake.ssh") version "2.9.0"
}

task<BuildPDFTask>("buildPdf") {
    group = "pdf"
    source = "main"
}

task<BuildHTMLTask>("buildHtml") {
    group = "html"
    source = "main"
    split = "chapter"
}

tasks.findByName("build")?.let {
    it.dependsOn(":buildPdf")
    it.dependsOn(":buildHtml")
}

apply(from = "deploy.gradle")