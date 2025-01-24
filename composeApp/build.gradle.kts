import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation("com.drewnoakes:metadata-extractor:2.18.0")
            implementation("com.huaweicloud:esdk-obs-java-bundle:3.24.12")
            implementation("androidx.datastore:datastore-preferences-core:1.1.2")
            implementation("io.coil-kt.coil3:coil-compose:3.0.4")
            implementation("com.google.code.gson:gson:2.11.0")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.equationl.hugo_gallery_uploader.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            // https://stackoverflow.com/questions/79177464/kmp-compose-multiplatform-datastore-not-working-on-jvm-release-build
            modules("jdk.unsupported")
            modules("jdk.unsupported.desktop")
            modules("java.management")

            packageName = "HugoGalleryUploader"
            packageVersion = "1.0.0"
            copyright = "© 2025 likehide.com. All rights reserved."
            vendor = "equationl"

            windows {
                menuGroup = "Likehide"
            }

            macOS {
                bundleID = "com.likehide.hugo_gallery_uploader"
            }
        }
    }
}
