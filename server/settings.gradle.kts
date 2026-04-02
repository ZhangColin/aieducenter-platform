rootProject.name = "aieducenter-server"

// 开发期联调：引用本地 cartisan-boot（通过 mavenLocal）
// includeBuild 与 java-platform 有兼容性问题，改用 mavenLocal()
// 路径：server/ → ../aieducenter-platform/ → ../workspace/ → cartisan-boot/
// includeBuild("../../cartisan-boot")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenLocal()  // 优先使用本地 Maven 仓库（cartisan-boot）
        mavenCentral()
    }
}
