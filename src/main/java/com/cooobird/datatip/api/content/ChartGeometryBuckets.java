package com.cooobird.datatip.api.content;

import java.util.ArrayList;
import java.util.List;

/**
 * 按实际可见像素聚合图表几何，语义条目本身保持完整。
 */
final class ChartGeometryBuckets {
    private ChartGeometryBuckets() {
    }

    static List<Bucket> maximumAbsolute(
        List<ChartContent.ChartEntry> entries,
        double[] values,
        int visiblePixels
    ) {
        return aggregate(entries, values, visiblePixels, Mode.MAXIMUM_ABSOLUTE);
    }

    static List<Bucket> average(
        List<ChartContent.ChartEntry> entries,
        double[] values,
        int visiblePixels
    ) {
        return aggregate(entries, values, visiblePixels, Mode.AVERAGE);
    }

    static List<Bucket> sumAbsolute(
        List<ChartContent.ChartEntry> entries,
        double[] values,
        int visiblePixels
    ) {
        return aggregate(entries, values, visiblePixels, Mode.SUM_ABSOLUTE);
    }

    private static List<Bucket> aggregate(
        List<ChartContent.ChartEntry> entries,
        double[] values,
        int visiblePixels,
        Mode mode
    ) {
        int count = entries.size();
        int bucketCount = Math.min(count, Math.max(1, visiblePixels));
        ArrayList<Bucket> result = new ArrayList<>(bucketCount);
        for (int bucket = 0; bucket < bucketCount; bucket++) {
            int start = (int) ((long) bucket * count / bucketCount);
            int end = (int) ((long) (bucket + 1) * count / bucketCount);
            double value = mode == Mode.MAXIMUM_ABSOLUTE
                ? values[start]
                : 0;
            for (int index = start; index < end; index++) {
                value = switch (mode) {
                    case MAXIMUM_ABSOLUTE -> Math.abs(values[index]) > Math.abs(value)
                        ? values[index]
                        : value;
                    case AVERAGE -> value + values[index];
                    case SUM_ABSOLUTE -> value + Math.abs(values[index]);
                };
            }
            if (mode == Mode.AVERAGE) value /= Math.max(1, end - start);
            result.add(new Bucket(value, entries.get(start).color()));
        }
        return List.copyOf(result);
    }

    record Bucket(double value, int color) {
    }

    private enum Mode {
        MAXIMUM_ABSOLUTE,
        AVERAGE,
        SUM_ABSOLUTE
    }
}
