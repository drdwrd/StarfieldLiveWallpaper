package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.matrix4f
import drwdrd.ktdev.engine.vector2f
import drwdrd.ktdev.engine.vector3f
import drwdrd.ktdev.engine.vector4f

class Sprite(_position : vector3f, _velocity : vector3f, _scale : Float, _uvPos : vector2f, _gammaBurst : vector3f, _age : Float) {

    val position = _position
    var velocity = _velocity
    var scale = _scale
    var uvPos = _uvPos
    var gammaBurst = _gammaBurst
    var age = _age

}