plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}



android {
    namespace = "ru.kondrashen.diplomappv20"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.kondrashen.diplomappv20"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas".toString())
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Основные зависимости
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.core:core-ktx:1.12.0")

    //Retrofit и карутины
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // ViewModel и Livedata
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Навигация
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.test.ext:junit-ktx:1.2.1")

    // Тестирование
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.mockito:mockito-inline:3.12.4")
    // Запуск фрагмента
    debugImplementation("androidx.fragment:fragment-testing:1.8.1")
    testImplementation("org.robolectric:robolectric:4.12.2")
    // Тестовая карутина
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    testImplementation("com.jraska.livedata:testing-ktx:1.3.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.test:core:1.2.0")
    testImplementation("androidx.test.ext:truth:1.2.0")
    testImplementation("com.google.truth:truth:0.44")

    //для Бд и Кэша
    implementation("androidx.room:room-common:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
//    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // real-time чат и push уведомления
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.google.firebase:firebase-analytics")

    //Зависимости для разметки
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Диаграммы
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Чтение PDF
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")

    // Такая себе карусель...
    implementation("com.github.sparrow007:carouselrecyclerview:1.2.6")

    // Спинер с поиском
    implementation("com.toptoche.searchablespinner:searchablespinnerlibrary:1.3.1")

    // Для подгрузки изображений
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Поддержка гифок
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.17")

}


