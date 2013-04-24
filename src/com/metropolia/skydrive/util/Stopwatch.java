package com.metropolia.skydrive.util;

/**
 * @author Anoja
 * Date: 21.04.13
 * Time: 23:58
 */

/**
 * This class provides the functionality for stop watch.
 */

public class Stopwatch
{
    private final long start;

    public Stopwatch()
    {
        start = System.currentTimeMillis();
    }

    public double elapsedTimeInSeconds()
    {
        return (System.currentTimeMillis() - start) / 1000d;
    }
}
