package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.matrix4f
import drwdrd.ktdev.engine.vector2f
import drwdrd.ktdev.engine.vector3f
import drwdrd.ktdev.engine.vector4f

class Sprite(_position : vector3f, _scale : vector2f, _uvPatch : vector4f) {
    var position = _position
    var scale = _scale
    var uvPatch = _uvPatch

    private val modelMatrix = matrix4f()
}