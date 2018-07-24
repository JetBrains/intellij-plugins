package com.testapp.components;

import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;

public class Count
{
    @Parameter
    private int start = 1;

    @Parameter(required = true)
    private int end;

    @Parameter
    private int value;


    private boolean increment;

    @SetupRender
    void initializeValue()
    {
        value = start;

        increment = start < end;
    }

    @AfterRender
    boolean next()
    {
        if (increment)
        {
            int newValue = value + 1;

            if (newValue <= end)
            {
                value = newValue;
                return false;
            }
        }
        else
        {
            int newValue = value - 1;

            if (newValue >= end)
            {
                value = newValue;
                return false;
            }
        }

        return true;
    }
}