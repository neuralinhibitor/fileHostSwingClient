package com.buzzard.fileservice;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.AxisFault;

import com.buzzard.etc.BuzzardConstants;
import com.buzzard.etc.Logger;
import com.buzzard.fileserver.service.FileServerServiceStub;
import com.buzzard.fileserver.service.FileServerServiceStub.AddUser;
import com.buzzard.fileserver.service.FileServerServiceStub.AddUserResponse;
import com.buzzard.fileserver.service.FileServerServiceStub.AuthenticationCredentials;
import com.buzzard.fileserver.service.FileServerServiceStub.DownloadFileChunk;
import com.buzzard.fileserver.service.FileServerServiceStub.DownloadFileChunkResponse;
import com.buzzard.fileserver.service.FileServerServiceStub.File;
import com.buzzard.fileserver.service.FileServerServiceStub.FileChunk;
import com.buzzard.fileserver.service.FileServerServiceStub.GetFileList;
import com.buzzard.fileserver.service.FileServerServiceStub.GetFileListResponse;
import com.buzzard.fileserver.service.FileServerServiceStub.UpdateUser;
import com.buzzard.fileserver.service.FileServerServiceStub.UpdateUserResponse;
import com.buzzard.fileserver.service.FileServerServiceStub.UploadFileChunk;


public class FileServiceCaller
{
  private final FileServerServiceStub service;
  private final AuthenticationCredentials credentials;
  
  public FileServiceCaller(
      String endpoint, 
      String username, 
      String password
      ) throws AxisFault
  {
    AuthenticationCredentials credentials = new AuthenticationCredentials();
    credentials.setUserName(username);
    credentials.setUserPassword(password);
    
    this.credentials = credentials;
    this.service = new FileServerServiceStub(null,endpoint);
    this.service._getServiceClient().getOptions().setTimeOutInMilliSeconds(BuzzardConstants.TIMEOUT_MS);
  }
  
  public boolean verifyOrCreateCredentials()
  {
    boolean credentialsValid = false;
    
    try
    {
      getFiles();
      credentialsValid = true;
    }
    catch(Exception e)
    {
      //do nothing
    }
    
    if(credentialsValid)
    {
      return true;
    }
    
    Logger.info(String.format("user does not exist, attempting to create"));
    
    return addUser();
  }
  
  public synchronized long[] getOffsets(String fileName)
  {
    File[] files = this.getFiles();
    
    File match = null;
    for(File file:files)
    {
      if(file.getFileName().equals(fileName))
      {
        match = file;
      }
    }
    
    return match.getChunkOffsets();
  }
  
  public synchronized byte[] getData(String fileName, long offset)
  {
    DownloadFileChunk arg = new DownloadFileChunk();
    arg.setCredentials(credentials);
    arg.setFileName(fileName);
    arg.setOffset(offset);
    
    DownloadFileChunkResponse response = null;
    byte[] data = null;
    
    try
    {
      response = service.downloadFileChunk(arg);
      
      InputStream input = response.getChunk().getData().getInputStream();
      List<Byte> bytes = new ArrayList<Byte>();
      
      boolean stop = false;
      while(!stop)
      {
        int value = input.read();
        
        if(value == -1)
        {
          stop = true;
        }
        else
        {
          bytes.add((byte)value);
        }
      }
      
      data = new byte[bytes.size()];
      
      for(int i=0; i<bytes.size(); i++)
      {
        data[i] = bytes.get(i);
      }
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
    
    return data;
  }
  
  public synchronized void uploadChunk(
      long offset, 
      byte[] data, 
      String fileName
      )
  {
    ByteArrayDataSource source = new ByteArrayDataSource(data);
    DataHandler dataHandler = new DataHandler(source);
    
    FileChunk chunk = new FileChunk();
    chunk.setData(dataHandler);
    chunk.setFileName(fileName);
    chunk.setOffset(offset);
    
    UploadFileChunk arg = new UploadFileChunk();
    arg.setChunk(chunk);
    arg.setCredentials(credentials);
    
    try
    {
      service.uploadFileChunk(arg);
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }
  
//  private boolean updateUser()
//  {
//    UpdateUser arg = new UpdateUser();
//    arg.setOldCredentials(credentials);
//    arg.setNewCredentials(credentials);
//    
//    boolean wasSuccess = false;
//    
//    try
//    {
//      UpdateUserResponse response = service.updateUser(arg);
//      wasSuccess = response.getWasSuccess();
//    }
//    catch(Exception e)
//    {
//      Logger.error(e.toString());
//    }
//    
//    return wasSuccess;
//  }
  
  private boolean addUser()
  {
    AddUser arg = new AddUser();
    arg.setCredentials(credentials);
    
    boolean wasSuccess = false;
    
    try
    {
      AddUserResponse response = service.addUser(arg);
      wasSuccess = response.getWasSuccess();
    }
    catch(Exception e)
    {
      Logger.error(e.toString());
    }
    
    Logger.info(String.format("user does not exist, attempting to create"));
    
    return wasSuccess;
  }
  
  public File[] getFiles()
  {
    GetFileList arg = new GetFileList();
    arg.setCredentials(credentials);
    
    GetFileListResponse response = null;
    
    try
    {
      response = service.getFileList(arg);
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
    
    if(response.getFiles() == null)
    {
      return new File[0];
    }
    
    return response.getFiles();
  }
  
}

