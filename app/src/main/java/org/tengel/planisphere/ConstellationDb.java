package org.tengel.planisphere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ConstellationDb
{
    class Constellation
    {
        String mName = new String();
        ArrayList<Catalog.Entry> mLine = new ArrayList<Catalog.Entry>();
    }

    private ArrayList<Constellation> mEntries = new ArrayList<Constellation>();
    private HashMap<String, String[]> mNames = new HashMap<String, String[]>();
    private HashMap<String, ArrayList<Double[]>> mBoundaries = new HashMap<>();
    private static ConstellationDb sInstance = null;

    public static ConstellationDb instance() throws NullPointerException
    {
        if (sInstance == null)
        {
            throw new NullPointerException("run init() before instance()");
        }
        return sInstance;
    }

    public synchronized static void init(InputStream lineStream, InputStream nameStream,
                                         InputStream boundStream,
                                         Catalog catalog) throws IOException
    {
        if (lineStream == null || nameStream == null || boundStream == null || catalog == null)
        {
            throw new NullPointerException("parameter must not be null");
        }
        else if(sInstance == null)
        {
            sInstance = new ConstellationDb(lineStream, nameStream, boundStream, catalog);
        }
    }


    private ConstellationDb(InputStream lineStream, InputStream nameStream,
                            InputStream boundStream,
                            Catalog catalog) throws IOException
    {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(lineStream));
        while (true)
        {
            String line = fileReader.readLine();
            if (line == null)
            {
                break;
            }
            if (line.startsWith("#") || line.length() == 0)
            {
                continue;
            }
            String[] lItems = line.split(" +");
            Constellation con = new Constellation();
            con.mName = lItems[0].trim().toLowerCase(Locale.ROOT);
            int pointId;
            for (int i = 2; i < lItems.length; ++i)
            {
                pointId = Integer.valueOf(lItems[i].trim());
                con.mLine.add(catalog.get(pointId));
            }
            mEntries.add(con);
        }

        fileReader = new BufferedReader(new InputStreamReader(nameStream));
        while (true)
        {
            String line = fileReader.readLine();
            if (line == null)
            {
                break;
            }
            String[] lItems = line.split("\t");
            mNames.put(lItems[0].trim().toLowerCase(Locale.ROOT),
                       new String[] {lItems[0].trim(), lItems[1].trim(),
                                     lItems[2].trim(), lItems[3].trim()});
        }

        fileReader = new BufferedReader(new InputStreamReader(boundStream));
        while(true)
        {
            String line = fileReader.readLine();
            if (line == null)
            {
                break;
            }
            String[] lItems = line.split("\\|");
            String rightAscension = lItems[0].trim();
            String[] raItems = rightAscension.split(" ");
            Integer raH = Integer.valueOf(raItems[0].trim());
            Integer raM = Integer.valueOf(raItems[1].trim());
            Double raS = Double.valueOf(raItems[2].trim());
            Double declination = Double.valueOf(lItems[1].trim());
            String name = lItems[2].trim().toLowerCase(Locale.ROOT);
            if (mBoundaries.get(name) == null)
            {
                mBoundaries.put(name, new ArrayList<Double[]>());
            }
            mBoundaries.get(name).add(new Double[] {raH + (raM / 60.0) + (raS / 60.0 / 60.0),
                                                    declination});
        }
    }

    public ArrayList<Constellation> get()
    {
        return mEntries;
    }

    public String getName(String abbr)
    {
        return mNames.get(abbr)[0];
    }

    public ArrayList<Double[]> getBoundary(String name)
    {
        return mBoundaries.get(name);
    }
}