package com.metropolia.skydrive.util;

/**
 * @author Anoja
 * Date: 16.04.13
 * Time: 22:05
 */

import java.util.Random;

/**
 * This utility class generates random number and converts bytes to giga-bytes.
 */

public class Utility
{
    public static int getRandomNuberFromRange(int from, int to)
    {
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        return from + r.nextInt(to - from);
    }

    public static int getRandomNumber()
    {
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        return r.nextInt();
    }

    public static int getRandomNumberFromRangeCustomSeed(int from, int to,
                                                         long seed)
    {
        Random r = new Random();
        r.setSeed(seed);
        return from + r.nextInt(to - from);
    }

    public static int getRandomNumberCustomSeed(long seed)
    {
        Random r = new Random();
        r.setSeed(seed);
        return r.nextInt();
    }

    public static long convertBytesToGigabytes(long bytes)
    {
        return bytes / 1024 / 1024 / 1024;
    }
}
