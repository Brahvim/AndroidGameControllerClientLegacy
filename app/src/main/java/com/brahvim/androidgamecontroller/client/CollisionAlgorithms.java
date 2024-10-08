package com.brahvim.androidgamecontroller.client;

import processing.core.PVector;

// Courtesy of [https://github.com/bmoren/p5.collide2D]
public class CollisionAlgorithms {
    public static boolean ptPoly(float p_x, float p_y, PVector[] p_poly) {
        // As seen on Sir Ben's GitHub!:
        /*
        p5.prototype.collidePointPoly = function(p_x, p_y, vertices) {
            var collision = false;

            // go through each of the vertices, plus the next vertex in the list
            var next = 0;
            for (var current = 0; current < vertices.length; current++) {

                // get next vertex in list if we've hit the end, wrap around to 0
                next = current + 1;
                if (next == = vertices.length) next = 0;

                // get the PVectors at our current position this makes our if statement a little
                // cleaner
                var vc = vertices[current];    // c for "current"
                var vn = vertices[next];       // n for "next"

                // compare position, flip 'collision' variable back and forth
                if (((vc.y >= p_y && vn.y < p_y) || (vc.y < p_y && vn.y >= p_y)) &&
                  (p_x < (vn.x - vc.x) * (p_y - vc.y) / (vn.y - vc.y) + vc.x)) {
                    collision = !collision;
                }
            }
            return collision;
        }
        */

        // region Here, CC-BY-NC-SA International 4.0, here's my version!:
        boolean ret = false;

        int next;
        for (int i = 0; i < p_poly.length; i++) {
            next = i + 1;

            if (next > p_poly.length)
                next = 0;

            PVector currentVertex = p_poly[i],
              nextVertex = p_poly[next];

            if (
              (
                (currentVertex.y >= p_y && nextVertex.y < p_y) ||
                  (currentVertex.y < p_y && nextVertex.y >= p_y)
              )
                &&
                (
                  p_x <
                    (nextVertex.x - currentVertex.x) * (p_y - currentVertex.y)
                      / (nextVertex.y - currentVertex.y) + currentVertex.x
                )
            ) {
                ret = !ret; // :rofl:
            }
        }

        return ret;
        // endregion

        // :D
    }

    // Anybody who needs to copy these? LOL:
    // (...well I did! Because I already wrote them LOL.
    // Take a look at `https://editor.p5js.org/Brahvim/sketches/hyoou0Gkb` :D)

    public static boolean ptCircle(PVector p_point, PVector p_circlePos, float p_radius) {
        return Math.pow(p_circlePos.x - p_point.x, 2)
          + Math.pow(p_circlePos.y - p_point.y, 2)
          < p_radius * p_radius;
    }

    public static boolean ptRectByDims(
      float p_vertX, float p_vertY,
      float p_rectStartX, float p_rectStartY,
      float p_rectWidth, float p_rectHeight) {
        return CollisionAlgorithms.ptRect(
          p_vertX, p_vertY,
          p_rectStartX, p_rectStartY,
          p_rectStartX + p_rectWidth,
          p_rectStartY + p_rectHeight);
    }

    public static boolean ptRectByDims(
      PVector p_vertex,
      PVector p_rectPos,
      PVector p_rectDims) {
        return CollisionAlgorithms.ptRect(
          p_vertex.x, p_vertex.y,
          p_rectPos.x, p_rectPos.y,
          p_rectPos.x + p_rectDims.x,
          p_rectPos.y + p_rectDims.y);
    }

    public static boolean ptRect(
      PVector p_vertex,
      PVector p_rectStart,
      PVector p_rectEnd) {
        return CollisionAlgorithms.ptRect(
          p_vertex.x, p_vertex.y,
          p_rectStart.x, p_rectStart.y,
          p_rectEnd.x, p_rectEnd.y);
    }

    public static boolean ptRect(
      float p_vertX, float p_vertY,
      float p_rectStartX, float p_rectStartY,
      float p_rectEndX, float p_rectEndY) {
        return p_vertX > p_rectStartX
          && p_vertX < p_rectEndX
          && p_vertY > p_rectStartY
          && p_vertY < p_rectEndY;
    }
}
