package com.ogos.apprandomizador

import android.app.Application
import com.ogos.apprandomizador.core.di.AppContainer

class RandomApplication : Application() {
    val container by lazy { AppContainer(this) }
}
