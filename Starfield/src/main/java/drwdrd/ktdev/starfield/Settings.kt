package drwdrd.ktdev.starfield

import java.lang.ref.WeakReference

object Settings {

    interface OnSettingsChangedListener {
        fun onTimeScaleChanged(timeScale : Double)
        fun onStarParticleSpawnTimeChanged(spawnTime : Double)
        fun onCloudParticleSpawnTimeChanged(spawnTime : Double)
        fun onParallaxEffectMultiplierChanged(multiplier : Float)
    }


    var timeScale = 1.0
        set(value) {
            onSettingsChangedListener?.get()?.onTimeScaleChanged(value)
            field = value
        }


    var starParticlesSpawnTime = 0.01
        set(value) {
            onSettingsChangedListener?.get()?.onStarParticleSpawnTimeChanged(value)
            field = value
        }

    var cloudParticleSpawnTime = 0.1
        set(value) {
            onSettingsChangedListener?.get()?.onCloudParticleSpawnTimeChanged(value)
            field = value
        }

    var parallaxEffectMultiplier = 1.0f
        set(value) {
            onSettingsChangedListener?.get()?.onParallaxEffectMultiplierChanged(value)
            field = value
        }


    var onSettingsChangedListener : WeakReference<OnSettingsChangedListener>? = null
}