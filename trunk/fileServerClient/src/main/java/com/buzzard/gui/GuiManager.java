package com.buzzard.gui;

import java.awt.EventQueue;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JProgressBar;

import com.buzzard.etc.Logger;
import com.buzzard.fileserver.service.FileServerServiceStub.File;
import com.buzzard.fileservice.FileServiceCaller;
import com.buzzard.fileservice.ProgressMeasurable;

public class GuiManager
{
  public FileServiceCaller service;
  
  
  private static class ServerManagerFrameRunner implements Runnable
  {
    public ServerManagerFrame frame;
    private final GuiManager manager;
    
    public ServerManagerFrameRunner(GuiManager manager)
    {
      this.manager = manager;
    }
    
    
    @Override
    public void run()
    {
      try
      {
        frame = new ServerManagerFrame(manager);
        frame.setVisible(true);
      } 
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

  }
  
  public boolean verifyCredentials(
      String endpoint, 
      String username, 
      String password
      )
  {
    boolean success = false;
    
    try
    {
      this.service = new FileServiceCaller(endpoint,username,password);
      success = this.service.verifyOrCreateCredentials();
    }
    catch(Exception e)
    {
      Logger.error(e.toString());
    }
    
    return success;
  }
  
  public void updateFileList(JComboBox comboBox)
  {
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    comboBox.setModel(model);
    
    File[] files = service.getFiles();
    
    Logger.info(String.format("found %d files for user",files.length));
    
    for(File file:files)
    {
      String tag = String.format(
          "%s\t(%d chunks)",
          file.getFileName(),
          file.getChunkOffsets().length);
      model.addElement(tag);
    }
  }
  
  public String getSelectedFileName(JComboBox comboBox)
  {
    String tag = (String)comboBox.getSelectedItem();
    
    String fileName = tag.split("\t")[0];
    
    return fileName;
  }
  
  
  public void nonBlockingProgressMonitor(
      final ProgressMeasurable operation, 
      final JProgressBar progressBar,
      final JButton...buttons
      )
  {
    Runnable runnable = new Runnable()
    {
      @Override
      public void run()
      {
        for(JButton button:buttons)
        {
          button.setEnabled(false);
        }
        
        boolean stop = false;
        int oldPercent = -1;
        while(!stop)
        {
          int percent = (int)Math.ceil(operation.getPercentComplete());
          
          if(oldPercent != percent)
          {
            Logger.info(String.format("\t%d%% complete",percent));
            oldPercent = percent;
            progressBar.setValue(percent);
            progressBar.repaint();
          }
          
          if(percent < 0)
          {
            progressBar.setIndeterminate(true);
          }
          
          try
          {
            Thread.sleep(50);
          }
          catch(Exception e)
          {
            Logger.error(e.toString());
            //do nothing
          }
          
          if(percent >= 100)
          {
            stop = true;
          }
        }
        
        for(JButton button:buttons)
        {
          button.setEnabled(true);
        }
        
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
      }
    };
    
    Thread monitor = new Thread(runnable);
    monitor.start();
  }
  
  
  

  public static void main(String[] args)
  {
    GuiManager manager = new GuiManager();
    
    ServerManagerFrameRunner managerFrame = 
        new ServerManagerFrameRunner(manager);
    
    EventQueue.invokeLater(managerFrame);
  }
  
  
}

