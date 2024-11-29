[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/passage?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/passage)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.7.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
![gradle-version](https://img.shields.io/badge/gradle-8.5.2-blue?logo=gradle)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

[![Website](https://img.shields.io/badge/Author-vivienmahe.com-purple)](https://vivienmahe.com/)
[![X/Twitter](https://img.shields.io/twitter/follow/VivienMahe)](https://twitter.com/VivienMahe)

<br>

![Passage logo](https://github.com/user-attachments/assets/c92f7d44-df02-4860-9ba4-fb4d4c3f3d68#gh-light-mode-only)
![Passage logo](https://github.com/user-attachments/assets/28914622-f8b9-403b-a1b6-3c736af5e98a#gh-dark-mode-only)

---

<h3>A Kotlin/Compose Multiplatform library for seamless authentication on Android and iOS.</h3>

Be sure to show your support by starring ⭐️ this repository, and feel free to [contribute](#-contributing) if you're interested!

## ⚙️ Setup

### Installation
In your `build.gradle.kts` file, add Maven Central to your repositories:
```Groovy
repositories {
    mavenCentral()
}
```

Then add Passage dependency to your module:

- With version catalog, open `libs.versions.toml`:
```Groovy
[versions]
passage = "1.2.0" // Check latest version

[libraries]
passage = { group = "io.github.tweener", name = "passage", version.ref = "passage" }
```

Then in your module `build.gradle.kts` add:
```Groovy
dependencies {
    implementation(libs.passage)
}
```

- Without version catalog, in your module `build.gradle.kts` add:
```Groovy
dependencies {
    val passage_version = "1.2.0" // Check latest version

    implementation("io.github.tweener:passage:$passage_version")
}
```

The latest version is: [![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/passage?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/passage)

## 👨‍💻 Contributing

We love your input and welcome any contributions! Please read our [contribution guidelines](https://github.com/Tweener/passage/blob/master/CONTRIBUTING.md) before submitting a pull request.

## 🙏 Credits

- Logo by [Freeicons](https://freeicons.io/essential-collection/alarm-icon-icon-2)

## 🪪 Licence
