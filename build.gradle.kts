import latexml.BuildHTMLTask
import latexml.BuildPDFTask
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.SessionHandler
import org.hidetake.groovy.ssh.core.Remote

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
}

tasks.findByName("build")?.let {
    it.dependsOn(":buildPdf")
    it.dependsOn(":buildHtml")
}

apply(from = "deploy.gradle")