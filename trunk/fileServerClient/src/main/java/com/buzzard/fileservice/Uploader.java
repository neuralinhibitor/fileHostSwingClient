package com.buzzard.fileservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.buzzard.etc.BuzzardConstants;
import com.buzzard.etc.Logger;
import com.buzzard.gui.GuiManager;



public class Uploader implements Runnable, ProgressMeasurable
{
  private final File file;
  private FileInputStream input;
  private final GuiManager manager;
  private double progressPercent;
  
  public static ProgressMeasurable uploadNonBlocking(
      String filePath, 
      GuiManager manager
      ) throws FileNotFoundException
  {
    Uploader uploader = new Uploader(filePath,manager);
    
    Thread thread = new Thread(uploader);
    thread.start();
    
    return uploader;
  }
  
  private Uploader(
      String filePath, 
      GuiManager manager
      ) throws FileNotFoundException
  {
    this.file = new File(filePath);
    this.manager = manager;
    this.input = new FileInputStream(file);
  }
  
  private static double getPercentComplete(long offset, long length)
  {
    return (100.0d*offset) / (1.0d*length);
  }
  
  private void uploadNonBlocking()
  {
    long length = file.length();
    
    try
    {
      long offset = 0l;
      boolean stop = false;
      while(!stop)
      {
        this.progressPercent = getPercentComplete(offset,length);
        
        byte[] chunk = new byte[BuzzardConstants.MAX_DATA_CHUNK_SIZE];
        
        int chunkLength = input.read(chunk);
        if(chunkLength == -1)
        {
          stop = true;
        }
        else
        {
          if(chunkLength < chunk.length)
          {
            byte[] newChunk = new byte[chunkLength];
            System.arraycopy(chunk, 0, newChunk, 0, chunkLength);
            chunk = newChunk;
          }
          
          manager.service.uploadChunk(offset, chunk, file.getName());
          
          offset += chunk.length;
        }
      }
      
      input.close();
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      this.progressPercent = 100.0d;
    }
    
    Logger.info(String.format("done uploading %s",file.getAbsolutePath()));
  }

  @Override
  public void run()
  {
    uploadNonBlocking();
  }

  @Override
  public double getPercentComplete()
  {
    return this.progressPercent;
  }
  
}

