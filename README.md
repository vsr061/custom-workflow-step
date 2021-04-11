# PDF Watermark Workflow Model & Step

This project is built to demonstrate how to create custom workflow steps and models in AEM.

**Use case:** Adding a watermark to a PDF using AEM Asset Workflow.

This project uses [Apache PDFBox library](https://pdfbox.apache.org/) to parse PDF documents and to add a watermark layer for each page. These dependencies are embedded in the project bundle JAR file **(Check core module's POM file for more details: core/pom.xml, line 67)**.

**Add PDF Watermark Model:** `/var/workflow/models/add-pdf-watermark`

**PDF Watermark Process Step Component:** `/apps/custom-workflow-step/components/workflow/pdf-watermark`

**PDF Watermark Process Step JAVA Class:** `com.hashout.core.workflows.PDFWatermarkProcess`

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with  

    mvn clean install -PautoInstallPackage

Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish

Or alternatively

    mvn clean install -PautoInstallPackage -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
