package com.buzzard.gui;


import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Cursor;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JProgressBar;

import com.buzzard.etc.Logger;
import com.buzzard.fileservice.Downloader;
import com.buzzard.fileservice.ProgressMeasurable;
import com.buzzard.fileservice.Uploader;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ServerManagerFrame extends JFrame
{
  private final GuiManager guiManager;

  private JTextField usernameField;
  private JPasswordField passwordField;
  private JTextField endpointField;
  private JButton downloadButton;
  private JButton uploadButton;
  private JProgressBar progressBar;
  private JComboBox fileList;
  
  
  public ServerManagerFrame(GuiManager guiManager)
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    setTitle("fs client");
    this.guiManager = guiManager;

    init();
  }

  private void init()
  {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 567, 178);
    getContentPane().setLayout(null);

    usernameField = new JTextField();
    usernameField.setText("root");
    usernameField.setBounds(313, 31, 238, 20);
    getContentPane().add(usernameField);
    usernameField.setColumns(10);

    JLabel lblUsername = new JLabel("username");
    lblUsername.setBounds(21, 34, 155, 14);
    getContentPane().add(lblUsername);

    passwordField = new JPasswordField();
    passwordField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          String endpoint = endpointField.getText();
          String userName = usernameField.getText();
          String password = new String(passwordField.getPassword());

          boolean verified = 
              guiManager.verifyCredentials(endpoint, userName,password);
          
          if (verified)
          {
            endpointField.setEnabled(false);
            usernameField.setEnabled(false);
            passwordField.setVisible(false);
            
            uploadButton.setEnabled(true);
            downloadButton.setEnabled(true);
            fileList.setEnabled(true);
            
            guiManager.updateFileList(fileList);
          }
          else
          {
            Logger.error(String.format("user verification failed"));
          }
        }
      }
    });

    JLabel lblPassword = new JLabel("password");
    lblPassword.setBounds(21, 55, 155, 14);
    getContentPane().add(lblPassword);

    final JComboBox fileComboBox = new JComboBox();
    fileComboBox.setEnabled(false);
    fileComboBox.setBounds(57, 76, 494, 20);
    getContentPane().add(fileComboBox);
    this.fileList = fileComboBox;

    JLabel lblFiles = new JLabel("files");
    lblFiles.setBounds(21, 79, 155, 14);
    getContentPane().add(lblFiles);

    final JButton uploadButton = new JButton("upload");
    uploadButton.setEnabled(false);
    uploadButton.setBounds(57, 104, 89, 23);
    getContentPane().add(uploadButton);
    this.uploadButton = uploadButton;
    
    uploadButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) 
      {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(uploadButton);
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
          String fileName = chooser.getSelectedFile().getAbsolutePath();
          
          try
          {
            ProgressMeasurable measurable = 
                Uploader.uploadNonBlocking(fileName, guiManager);
            guiManager.nonBlockingProgressMonitor(
                measurable, 
                progressBar,
                uploadButton,downloadButton);
            
            guiManager.updateFileList(fileList);
          }
          catch(Exception e)
          {
            throw new RuntimeException(e);
          }
        }
      }
    });
    

    final JButton downloadButton = new JButton("download");
    downloadButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(uploadButton);
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
          String dirPath = chooser.getSelectedFile().getAbsolutePath();
          String fileName = guiManager.getSelectedFileName(fileComboBox);
          
          try
          {
            ProgressMeasurable progress = 
                Downloader.downloadNonBlocking(dirPath,fileName,guiManager);
            
            guiManager.nonBlockingProgressMonitor(
                progress, 
                progressBar,
                uploadButton,downloadButton);
          }
          catch(Exception e)
          {
            throw new RuntimeException(e);
          }
        }
      }
    });
    
    downloadButton.setEnabled(false);
    downloadButton.setBounds(462, 104, 89, 23);
    getContentPane().add(downloadButton);
    this.downloadButton = downloadButton;

    JProgressBar progressBar = new JProgressBar();
    progressBar.setBounds(0, 131, 551, 9);
    getContentPane().add(progressBar);
    this.progressBar = progressBar;

    endpointField = new JTextField();
    endpointField.setText("http://localhost:8080/FileServerService/services/FileServerService");
    endpointField.setColumns(10);
    endpointField.setBounds(75, 0, 476, 20);
    getContentPane().add(endpointField);

    JLabel lblEndpoint = new JLabel("endpoint");
    lblEndpoint.setBounds(21, 3, 155, 14);
    getContentPane().add(lblEndpoint);
    // progressBar.setIndeterminate(true);
    
    passwordField.setBounds(313, 52, 238, 20);
    getContentPane().add(passwordField);
  }
  
  
}


