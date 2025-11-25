package xyz.jinenze.wuhumc.client.gui;

import java.util.function.Function;

public interface EasingMethod {
    double apply(double var1);

    enum EasingMethodImpl implements EasingMethod {
        LINEAR((v) -> v),
        EXPO((v) -> v == 1.0 ? 1.0 : 1.0 - Math.pow(2, -10 * v)),
        QUAD((v) -> v * v),
        QUART((v) -> v * v * v * v),
        SINE((v) -> Math.sin(v * (Math.PI / 2))),
        CUBIC((v) -> v * v * v),
        QUINTIC((v) -> v * v * v * v * v),
        CIRC((v) -> 1.0 - Math.sqrt(1.0 - v * v));

        private final Function<Double, Double> function;

        EasingMethodImpl(Function<Double, Double> function) {
            this.function = function;
        }

        public double apply(double v) {
            return this.function.apply(v);
        }

        public String toString() {
            return this.name();
        }
    }
}
