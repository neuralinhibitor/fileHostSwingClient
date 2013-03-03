package com.buzzard.etc;



public class Logger
{
  public static void error(String message)
  {
    System.out.printf("ERROR: %s\n",message);
  }
  
  public static void info(String message)
  {
    System.out.printf("INFO: %s\n",message);
  }
  
}