package com.streamflix.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * Icons - small vector icons drawn with JavaFX shapes (no image files needed, they scale perfectly).
 *
 * PORTION: Custom UI components  |  OWNER: Moeti Leleka
 */
public final class Icons {

    private Icons() { }

    /** Right-pointing triangle. */
    public static Node play(double size, Color color) {
        Polygon p = new Polygon(
                size * 0.15, 0,
                size * 0.95, size * 0.5,
                size * 0.15, size);
        p.setFill(color);
        return new Group(p);
    }

    /** Two vertical bars. */
    public static Node pause(double size, Color color) {
        Rectangle a = new Rectangle(size * 0.1, 0, size * 0.28, size);
        Rectangle b = new Rectangle(size * 0.62, 0, size * 0.28, size);
        a.setFill(color);
        b.setFill(color);
        a.setArcWidth(3);
        a.setArcHeight(3);
        b.setArcWidth(3);
        b.setArcHeight(3);
        return new Group(a, b);
    }

    /** Chevron arrow; direction = -1 for left, +1 for right. */
    public static Node chevron(double size, Color color, int direction) {
        Polyline line = direction < 0
                ? new Polyline(size * 0.7, 0, size * 0.25, size * 0.5, size * 0.7, size)
                : new Polyline(size * 0.3, 0, size * 0.75, size * 0.5, size * 0.3, size);
        line.setStroke(color);
        line.setStrokeWidth(size * 0.16);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setFill(null);
        return new Group(line);
    }

    /** Loudspeaker; on = sound waves, off = a cross. */
    public static Node speaker(double size, Color color, boolean on) {
        Polygon body = new Polygon(
                0, size * 0.35,
                size * 0.25, size * 0.35,
                size * 0.5, size * 0.1,
                size * 0.5, size * 0.9,
                size * 0.25, size * 0.65,
                0, size * 0.65);
        body.setFill(color);
        Group g = new Group(body);

        if (on) {
            g.getChildren().addAll(wave(size, color, 0.20), wave(size, color, 0.36));
        } else {
            Line l1 = new Line(size * 0.65, size * 0.33, size * 1.0, size * 0.67);
            Line l2 = new Line(size * 1.0, size * 0.33, size * 0.65, size * 0.67);
            for (Line l : new Line[]{l1, l2}) {
                l.setStroke(color);
                l.setStrokeWidth(size * 0.1);
                l.setStrokeLineCap(StrokeLineCap.ROUND);
            }
            g.getChildren().addAll(l1, l2);
        }
        return g;
    }

    private static Arc wave(double size, Color color, double radiusFactor) {
        Arc arc = new Arc(size * 0.5, size * 0.5, size * radiusFactor * 1.4, size * radiusFactor * 1.4, -45, 90);
        arc.setType(ArcType.OPEN);
        arc.setFill(null);
        arc.setStroke(color);
        arc.setStrokeWidth(size * 0.09);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        return arc;
    }
}
