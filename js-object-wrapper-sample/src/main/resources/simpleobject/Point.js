class Point {
    x = 0
    y = 0

    constructor(x, y) {
        this.x = x
        this.y = y
    }

    isOrigin() {
        return this.x == 0 && this.y == 0
    }

    plus(point) {
        return new Point(this.x + point.x, this.y+point.y)
    }

    move(x, y) {
        this.x = this.x + x
        this.y = this.y + y
        return this
    }

    toString() {
        return "Point@(" + this.x + "," + this.y + ")"
    }
}