package de.manuelgilles.knit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform