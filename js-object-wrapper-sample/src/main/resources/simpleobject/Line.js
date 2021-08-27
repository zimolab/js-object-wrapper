class Line {
    start = new Point(0, 0)
    end = new Point(0, 0)

    constructor(start, end) {
        this.start = start
        this.end = end
    }

    length() {
        let m = Math.pow(Math.abs(this.start.x - this.end.x), 2) + Math.pow(Math.abs(this.start.y - this.end.y), 2)
        return Math.abs(Math.sqrt(m))
    }

    contains(point) {
        let maxx = this.start.x > this.end.x ? this.start.x : this.end.x
        let minx = this.start.x > this.end.x ? this.end.x : this.start.x
        let maxy = this.start.y > this.end.y ? this.start.y : this.end.y
        let miny = this.start.y > this.end.y ? this.end.y : this.start.y

        if (((point.x - this.start.x) * (this.end.y - this.start.y) == (this.end.x - this.start.x) * (point.y - this.start.y)) && (point.x >= minx && point.x <= maxx) && (point.y >= miny && point.y <= maxy))
            return true;
        else
            return false;

    }

    toString() {
        return "Line@{(" + this.start.x + "," + this.start.y + "),(" + this.end.x + "," + this.end.y + ")}"
    }
}