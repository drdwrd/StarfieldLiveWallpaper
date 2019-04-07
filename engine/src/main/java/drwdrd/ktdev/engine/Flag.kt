package drwdrd.ktdev.engine

abstract class FlagType {
    abstract val type : Int
}

class Flag<T : FlagType> {

    constructor() {
        flags = 0
    }

    constructor(flag: T) {
        flags = flag.type
    }

    constructor(_flags : Int) {
        flags = _flags
    }

    var flags : Int = 0
        private set

    fun isFlag(t : T) : Boolean {
        return (flags == t.type)
    }

    fun hasFlag(t : T) : Boolean {
        return ((flags and t.type) == t.type)
    }

    fun setFlag(t : T) {
        flags = flags or t.type
    }

    fun unsetFlag(t : T) {
        flags = flags and t.type.inv()
    }
}
