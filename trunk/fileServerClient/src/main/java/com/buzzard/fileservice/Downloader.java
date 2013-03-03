package com.buzzard.fileservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.buzzard.etc.Logger;
import com.buzzard.gui.GuiManager;



public class Downloader implements Runnable, ProgressMeasurable
{
  private final File file;
  private final FileOutputStream output;
  private final GuiManager manager;
  private double progressPercent = 0.0d;
  
  public static ProgressMeasurable downloadNonBlocking(
      String dirPath,
      String fileName, 
      GuiManager manager
      ) throws FileNotFoundException
  {
    File file = new File(String.format("%s/%s",dirPath,fileName));
    
    Downloader downloader = new Downloader(file,manager);
    
    Thread thread = new Thread(downloader);
    thread.start();
    
    return downloader;
  }
  
  private Downloader(
      File file, 
      GuiManager manager
      ) throws FileNotFoundException
  {
    this.file = file;
    this.manager = manager;
    this.output = new FileOutputStream(file);
  }
  
  private static double getPercentComplete(int chunkNumber, int numChunks)
  {
    return (100.0d*chunkNumber) / (1.0d*numChunks);
  }
  
  private void downloadNonBlocking()
  {
    
    try
    {
      long[] offsets = manager.service.getOffsets(file.getName());
      
      int count = 0;
      for(long offset:offsets)
      {
        byte[] chunk = manager.service.getData(file.getName(), offset);
        output.write(chunk);
        
        this.progressPercent = getPercentComplete(count,offsets.length);
        
        count++;
      } 
      
      output.flush();
      output.close();
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      this.progressPercent = 100.0d;
    }
    
    Logger.info(String.format("done downloading %s",file.getAbsolutePath()));
  }

  @Override
  public void run()
  {
    downloadNonBlocking();
  }

  @Override
  public double getPercentComplete()
  {
    return this.progressPercent;
  }
  
}

